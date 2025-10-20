/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.testing.junit.http;

import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.CAPTURE_HEADERS;
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.CAPTURE_PARAMETERS;
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.EXCEPTION;
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.INDEXED_CHILD;
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.NOT_FOUND;
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.PATH_PARAM;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static java.util.Collections.singletonList;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.api.internal.HttpConstants;
import io.opentelemetry.instrumentation.testing.GlobalTraceUtil;
import io.opentelemetry.instrumentation.testing.util.ThrowingRunnable;
import io.opentelemetry.instrumentation.testing.util.ThrowingSupplier;
import io.opentelemetry.sdk.testing.assertj.SpanDataAssert;
import io.opentelemetry.sdk.testing.assertj.TraceAssert;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.semconv.ClientAttributes;
import io.opentelemetry.semconv.ErrorAttributes;
import io.opentelemetry.semconv.HttpAttributes;
import io.opentelemetry.semconv.NetworkAttributes;
import io.opentelemetry.semconv.ServerAttributes;
import io.opentelemetry.semconv.UrlAttributes;
import io.opentelemetry.semconv.UserAgentAttributes;
import io.opentelemetry.testing.internal.armeria.common.AggregatedHttpRequest;
import io.opentelemetry.testing.internal.armeria.common.AggregatedHttpResponse;
import io.opentelemetry.testing.internal.armeria.common.HttpMethod;
import io.opentelemetry.testing.internal.io.netty.bootstrap.Bootstrap;
import io.opentelemetry.testing.internal.io.netty.channel.ChannelInitializer;
import io.opentelemetry.testing.internal.io.netty.channel.ChannelOption;
import io.opentelemetry.testing.internal.io.netty.channel.ChannelPipeline;
import io.opentelemetry.testing.internal.io.netty.channel.EventLoopGroup;
import io.opentelemetry.testing.internal.io.netty.channel.socket.SocketChannel;
import io.opentelemetry.testing.internal.io.netty.channel.socket.nio.NioSocketChannel;
import io.opentelemetry.testing.internal.io.netty.handler.codec.http.HttpClientCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.assertj.core.api.AssertAccess;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

@SuppressWarnings("unused")
public abstract class AbstractHttpServerTest<SERVER> extends AbstractHttpServerUsingTest<SERVER> {
  public static final String TEST_REQUEST_HEADER = "X-Test-Request";
  public static final String TEST_RESPONSE_HEADER = "X-Test-Response";

  private final HttpServerTestOptions options = new HttpServerTestOptions();

  @BeforeAll
  void setupOptions() {
    options.expectedServerSpanNameMapper = this::expectedServerSpanName;
    options.expectedHttpRoute = this::expectedHttpRoute;

    configure(options);

    startServer();
  }

  @AfterAll
  void cleanup() {
    cleanupServer();
  }

  @Override
  protected final String getContextPath() {
    return options.contextPath;
  }

  protected void configure(HttpServerTestOptions options) {}

  public static <T, E extends Throwable> T controller(
      ServerEndpoint endpoint, ThrowingSupplier<T, E> closure) throws E {
    assert Span.current().getSpanContext().isValid() : "Controller should have a parent span.";
    if (endpoint == NOT_FOUND) {
      return closure.get();
    }
    return GlobalTraceUtil.runWithSpan("controller", closure);
  }

  public static <E extends Throwable> void controller(
      ServerEndpoint endpoint, ThrowingRunnable<E> closure) throws E {
    controller(
        endpoint,
        () -> {
          closure.run();
          return null;
        });
  }

  protected AggregatedHttpRequest request(ServerEndpoint uri, String method) {
    return AggregatedHttpRequest.of(
        HttpMethod.valueOf(method), resolveAddress(uri, getProtocolPrefix()));
  }

  private String getProtocolPrefix() {
    return options.useHttp2 ? "h2c://" : "h1c://";
  }

  private static Bootstrap buildBootstrap(EventLoopGroup eventLoopGroup) {
    Bootstrap bootstrap = new Bootstrap();
    bootstrap
        .group(eventLoopGroup)
        .channel(NioSocketChannel.class)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(10))
        .handler(
            new ChannelInitializer<SocketChannel>() {
              @Override
              protected void initChannel(SocketChannel socketChannel) {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(new HttpClientCodec());
              }
            });
    return bootstrap;
  }

  protected void assertHighConcurrency(int count) {
    ServerEndpoint endpoint = INDEXED_CHILD;
    List<Consumer<TraceAssert>> assertions = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      assertions.add(
          trace -> {
            SpanData rootSpan = trace.getSpan(0);
            // Traces can be in arbitrary order, let us find out the request id of the current one
            int requestId = Integer.parseInt(rootSpan.getName().substring("client ".length()));

            List<Consumer<SpanDataAssert>> spanAssertions = new ArrayList<>();
            spanAssertions.add(
                span ->
                    span.hasName(rootSpan.getName())
                        .hasKind(SpanKind.INTERNAL)
                        .hasNoParent()
                        .hasAttributesSatisfyingExactly(
                            equalTo(
                                AttributeKey.longKey(ServerEndpoint.ID_ATTRIBUTE_NAME),
                                requestId)));
            spanAssertions.add(
                span -> assertIndexedServerSpan(span, requestId).hasParent(rootSpan));

            if (options.hasHandlerSpan.test(endpoint)) {
              spanAssertions.add(
                  span -> assertHandlerSpan(span, "GET", endpoint).hasParent(trace.getSpan(1)));
            }

            int parentIndex = spanAssertions.size() - 1;
            spanAssertions.add(
                span ->
                    assertIndexedControllerSpan(span, requestId)
                        .hasParent(trace.getSpan(parentIndex)));

            trace.hasSpansSatisfyingExactly(spanAssertions);
          });
    }
    testing.waitAndAssertTraces(assertions);
  }

  protected String assertResponseHasCustomizedHeaders(
      AggregatedHttpResponse response, ServerEndpoint endpoint, String expectedTraceId) {
    if (!options.hasResponseCustomizer.test(endpoint)) {
      return null;
    }

    String responseHeaderTraceId = response.headers().get("x-test-traceid");
    String responseHeaderSpanId = response.headers().get("x-test-spanid");

    if (expectedTraceId != null) {
      assertThat(responseHeaderTraceId).matches(expectedTraceId);
    } else {
      assertThat(responseHeaderTraceId).isNotNull();
    }

    assertThat(responseHeaderSpanId).isNotNull();
    return responseHeaderSpanId;
  }

  // NOTE: this method does not currently implement asserting all the span types that groovy
  // HttpServerTest does
  protected void assertTheTraces(
      int size,
      String traceId,
      String parentId,
      String spanId,
      String method,
      ServerEndpoint endpoint) {
    List<Consumer<TraceAssert>> assertions = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      assertions.add(
          trace -> {
            List<Consumer<SpanDataAssert>> spanAssertions = new ArrayList<>();
            spanAssertions.add(
                span -> {
                  assertServerSpan(span, method, endpoint, endpoint.status);
                  if (traceId != null) {
                    span.hasTraceId(traceId);
                  }
                  if (spanId != null) {
                    span.hasSpanId(spanId);
                  }
                  if (parentId != null) {
                    span.hasParentSpanId(parentId);
                  } else {
                    span.hasNoParent();
                  }
                });

            if (options.hasHandlerSpan.test(endpoint)) {
              spanAssertions.add(
                  span -> {
                    assertHandlerSpan(span, method, endpoint);
                    span.hasParent(trace.getSpan(0));
                  });
            }

            if (endpoint != NOT_FOUND) {
              int parentIndex = 0;
              if (options.hasHandlerSpan.test(endpoint)) {
                parentIndex = spanAssertions.size() - 1;
              }
              int finalParentIndex = parentIndex;
              spanAssertions.add(
                  span -> {
                    assertControllerSpan(
                        span, endpoint == EXCEPTION ? options.expectedException : null);
                    span.hasParent(trace.getSpan(finalParentIndex));
                  });
              if (options.hasRenderSpan.test(endpoint)) {
                spanAssertions.add(span -> assertRenderSpan(span, method, endpoint));
              }
            }

            if (options.hasResponseSpan.test(endpoint)) {
              int parentIndex = spanAssertions.size() - 1;
              spanAssertions.add(
                  span ->
                      assertResponseSpan(
                          span, trace.getSpan(parentIndex), trace.getSpan(0), method, endpoint));
            }

            if (options.hasErrorPageSpans.test(endpoint)) {
              spanAssertions.addAll(errorPageSpanAssertions(method, endpoint));
            }

            trace.hasSpansSatisfyingExactly(spanAssertions);

            if (options.verifyServerSpanEndTime) {
              List<SpanData> spanData = AssertAccess.getActual(trace);
              if (spanData.size() > 1) {
                SpanData rootSpan = spanData.get(0);
                for (int j = 1; j < spanData.size(); j++) {
                  assertThat(rootSpan.getEndEpochNanos())
                      .isGreaterThanOrEqualTo(spanData.get(j).getEndEpochNanos());
                }
              }
            }
          });
    }

    testing.waitAndAssertTraces(assertions);
  }

  @CanIgnoreReturnValue
  protected SpanDataAssert assertControllerSpan(SpanDataAssert span, Throwable expectedException) {
    span.hasName("controller").hasKind(SpanKind.INTERNAL);
    if (expectedException != null) {
      span.hasStatus(StatusData.error());
      span.hasException(expectedException);
    }
    return span;
  }

  protected SpanDataAssert assertHandlerSpan(
      SpanDataAssert span, String method, ServerEndpoint endpoint) {
    throw new UnsupportedOperationException(
        "assertHandlerSpan not implemented in " + getClass().getName());
  }

  @CanIgnoreReturnValue
  protected SpanDataAssert assertResponseSpan(
      SpanDataAssert span,
      SpanData controllerSpan,
      SpanData handlerSpan,
      String method,
      ServerEndpoint endpoint) {
    return assertResponseSpan(span, controllerSpan, method, endpoint);
  }

  @CanIgnoreReturnValue
  protected SpanDataAssert assertResponseSpan(
      SpanDataAssert span, SpanData parentSpan, String method, ServerEndpoint endpoint) {
    span.hasParent(parentSpan);
    return assertResponseSpan(span, method, endpoint);
  }

  protected SpanDataAssert assertResponseSpan(
      SpanDataAssert span, String method, ServerEndpoint endpoint) {
    throw new UnsupportedOperationException(
        "assertResponseSpan not implemented in " + getClass().getName());
  }

  protected SpanDataAssert assertRenderSpan(
      SpanDataAssert span, String method, ServerEndpoint endpoint) {
    throw new UnsupportedOperationException(
        "assertRenderSpan not implemented in " + getClass().getName());
  }

  protected List<Consumer<SpanDataAssert>> errorPageSpanAssertions(
      String method, ServerEndpoint endpoint) {
    throw new UnsupportedOperationException(
        "errorPageSpanAssertions not implemented in " + getClass().getName());
  }

  @CanIgnoreReturnValue
  protected SpanDataAssert assertServerSpan(
      SpanDataAssert span, String method, ServerEndpoint endpoint, int statusCode) {

    Set<AttributeKey<?>> httpAttributes = options.httpAttributes.apply(endpoint);
    String expectedRoute = options.expectedHttpRoute.apply(endpoint, method);
    String name = options.expectedServerSpanNameMapper.apply(endpoint, method, expectedRoute);

    span.hasName(name).hasKind(SpanKind.SERVER);
    if (statusCode >= 500) {
      span.hasStatus(StatusData.error());
    }

    if (endpoint == EXCEPTION && options.hasExceptionOnServerSpan.test(endpoint)) {
      span.hasException(options.expectedException);
    }

    span.hasAttributesSatisfying(
        attrs -> {
          // we're opting out of these attributes in the new semconv
          assertThat(attrs)
              .doesNotContainKey(NetworkAttributes.NETWORK_TRANSPORT)
              .doesNotContainKey(NetworkAttributes.NETWORK_TYPE)
              .doesNotContainKey(NetworkAttributes.NETWORK_PROTOCOL_NAME);

          if (attrs.get(NetworkAttributes.NETWORK_PROTOCOL_VERSION) != null) {
            assertThat(attrs)
                .containsEntry(
                    NetworkAttributes.NETWORK_PROTOCOL_VERSION, options.useHttp2 ? "2" : "1.1");
          }

          assertThat(attrs).containsEntry(ServerAttributes.SERVER_ADDRESS, "localhost");
          // TODO: Move to test knob rather than always treating as optional
          // TODO: once httpAttributes test knob is used, verify default port values
          if (attrs.get(ServerAttributes.SERVER_PORT) != null) {
            assertThat(attrs).containsEntry(ServerAttributes.SERVER_PORT, port);
          }

          if (attrs.get(NetworkAttributes.NETWORK_PEER_ADDRESS) != null) {
            assertThat(attrs)
                .containsEntry(
                    NetworkAttributes.NETWORK_PEER_ADDRESS, options.sockPeerAddr.apply(endpoint));
          }
          if (attrs.get(NetworkAttributes.NETWORK_PEER_PORT) != null) {
            assertThat(attrs)
                .hasEntrySatisfying(
                    NetworkAttributes.NETWORK_PEER_PORT,
                    value ->
                        assertThat(value)
                            .isInstanceOf(Long.class)
                            .isNotEqualTo(Long.valueOf(port)));
          }

          assertThat(attrs).containsEntry(ClientAttributes.CLIENT_ADDRESS, TEST_CLIENT_IP);
          // client.port is opt-in
          assertThat(attrs).doesNotContainKey(ClientAttributes.CLIENT_PORT);

          assertThat(attrs).containsEntry(HttpAttributes.HTTP_REQUEST_METHOD, method);

          assertThat(attrs).containsEntry(HttpAttributes.HTTP_RESPONSE_STATUS_CODE, statusCode);
          if (statusCode >= 500) {
            assertThat(attrs).containsEntry(ErrorAttributes.ERROR_TYPE, String.valueOf(statusCode));
          }

          assertThat(attrs).containsEntry(UserAgentAttributes.USER_AGENT_ORIGINAL, TEST_USER_AGENT);

          assertThat(attrs).containsEntry(UrlAttributes.URL_SCHEME, "http");
          if (endpoint != INDEXED_CHILD) {
            assertThat(attrs)
                .containsEntry(UrlAttributes.URL_PATH, endpoint.resolvePath(address).getPath());
            if (endpoint.getQuery() != null) {
              assertThat(attrs).containsEntry(UrlAttributes.URL_QUERY, endpoint.getQuery());
            }
          }

          if (httpAttributes.contains(HttpAttributes.HTTP_ROUTE) && expectedRoute != null) {
            assertThat(attrs).containsEntry(HttpAttributes.HTTP_ROUTE, expectedRoute);
          }

          if (endpoint == CAPTURE_HEADERS) {
            assertThat(attrs)
                .containsEntry(
                    stringArrayKey("http.request.header.x-test-request"), singletonList("test"));
            assertThat(attrs)
                .containsEntry(
                    stringArrayKey("http.response.header.x-test-response"), singletonList("test"));
          }
          if (endpoint == CAPTURE_PARAMETERS) {
            assertThat(attrs)
                .containsEntry(
                    stringArrayKey("servlet.request.parameter.test-parameter"),
                    singletonList("test value õäöü"));
          }
        });

    return span;
  }

  @CanIgnoreReturnValue
  protected SpanDataAssert assertIndexedServerSpan(SpanDataAssert span, int requestId) {
    ServerEndpoint endpoint = INDEXED_CHILD;
    String method = "GET";
    return assertServerSpan(span, method, endpoint, endpoint.status)
        .hasAttributesSatisfying(
            equalTo(UrlAttributes.URL_PATH, endpoint.resolvePath(address).getPath()),
            equalTo(UrlAttributes.URL_QUERY, "id=" + requestId));
  }

  @CanIgnoreReturnValue
  protected SpanDataAssert assertIndexedControllerSpan(SpanDataAssert span, int requestId) {
    span.hasName("controller")
        .hasKind(SpanKind.INTERNAL)
        .hasAttributesSatisfyingExactly(
            equalTo(AttributeKey.longKey(ServerEndpoint.ID_ATTRIBUTE_NAME), requestId));
    return span;
  }

  public String expectedServerSpanName(
      ServerEndpoint endpoint, String method, @Nullable String route) {
    return HttpServerTestOptions.DEFAULT_EXPECTED_SERVER_SPAN_NAME_MAPPER.apply(
        endpoint, method, route);
  }

  public final boolean hasHttpRouteAttribute(ServerEndpoint endpoint) {
    return options.httpAttributes.apply(endpoint).contains(HttpAttributes.HTTP_ROUTE);
  }

  public final boolean hasHandlerSpan(ServerEndpoint endpoint) {
    return options.hasHandlerSpan.test(endpoint);
  }

  public String expectedHttpRoute(ServerEndpoint endpoint, String method) {
    // no need to compute route if we're not expecting it
    if (!hasHttpRouteAttribute(endpoint)) {
      return null;
    }

    if (HttpConstants._OTHER.equals(method)) {
      return null;
    }

    if (NOT_FOUND.equals(endpoint)) {
      return null;
    } else if (PATH_PARAM.equals(endpoint)) {
      return options.contextPath + "/path/:id/param";
    } else {
      return endpoint.resolvePath(address).getPath();
    }
  }
}
