/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.spring.webflux.v5_0.server.security;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.satisfies;
import static io.opentelemetry.semconv.HttpAttributes.HTTP_REQUEST_METHOD;
import static io.opentelemetry.semconv.HttpAttributes.HTTP_RESPONSE_STATUS_CODE;
import static io.opentelemetry.semconv.HttpAttributes.HTTP_ROUTE;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.testing.junit.AgentInstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.testing.internal.armeria.client.WebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test to verify that HTTP route is correctly captured when Spring Security is enabled with
 * WebFlux.
 *
 * <p>This test reproduces the issue reported at:
 * https://github.com/microsoft/ApplicationInsights-Java/issues/4554
 *
 * <p>When Spring Security is enabled, the http.route attribute is missing from spans, causing the
 * span name to be just "GET" instead of "GET /secured/test" or "GET /unsecured/test".
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {
      SpringWebFluxSecurityTestApplication.class,
      TestSecurityConfig.class,
      TestController.class,
      SpringWebfluxSecurityTest.ForceNettyAutoConfiguration.class
    })
class SpringWebfluxSecurityTest {

  @TestConfiguration
  static class ForceNettyAutoConfiguration {
    @Bean
    NettyReactiveWebServerFactory nettyFactory() {
      return new NettyReactiveWebServerFactory();
    }
  }

  @RegisterExtension
  static final InstrumentationExtension testing = AgentInstrumentationExtension.create();

  @Value("${local.server.port}")
  private int port;

  private WebClient client;

  @BeforeEach
  void beforeEach() {
    client = WebClient.builder("h1c://localhost:" + port).followRedirects().build();
  }

  @Test
  void unsecuredEndpointShouldHaveRouteInSpanName() {
    client.get("/unsecured/test").aggregate().join();

    // Unsecured endpoint should return 200 - expect 2 spans: SERVER and controller handler
    testing.waitAndAssertTraces(
        trace ->
            trace.hasSpansSatisfyingExactly(
                span ->
                    span.hasName("GET /unsecured/test")
                        .hasKind(SpanKind.SERVER)
                        .hasAttributesSatisfying(
                            equalTo(HTTP_REQUEST_METHOD, "GET"),
                            satisfies(HTTP_RESPONSE_STATUS_CODE, val -> val.isEqualTo(200)),
                            equalTo(HTTP_ROUTE, "/unsecured/test")),
                span ->
                    span.hasName("TestController.unsecuredEndpoint")
                        .hasKind(SpanKind.INTERNAL)));
  }

  /**
   * This test demonstrates the bug: when Spring Security is enabled, the http.route is not
   * captured for secured endpoints when access is denied, causing the span name to be just "GET"
   * instead of "GET /secured/test".
   *
   * <p>Expected behavior: span name should be "GET /secured/test" with http.route="/secured/test"
   * Actual behavior: span name is "GET" with http.route missing
   */
  @Test
  void securedEndpointShouldHaveRouteInSpanName() {
    // This endpoint requires authentication, so we expect a 401 response
    // But even for unauthenticated requests, the route should be captured
    client.get("/secured/test").aggregate().join();

    // When Spring Security denies the request (401), there is only 1 SERVER span
    // (no controller handler span since the controller is never invoked)
    testing.waitAndAssertTraces(
        trace ->
            trace.hasSpansSatisfyingExactly(
                span ->
                    // The bug: when Spring Security denies the request before the controller
                    // processes it, the http.route attribute is missing and span name is just "GET"
                    // This assertion is expected to FAIL, demonstrating the bug
                    span.hasName("GET /secured/test")
                        .hasKind(SpanKind.SERVER)
                        .hasAttributesSatisfying(
                            equalTo(HTTP_REQUEST_METHOD, "GET"),
                            // The response should be 401 Unauthorized
                            satisfies(HTTP_RESPONSE_STATUS_CODE, val -> val.isEqualTo(401)),
                            // This is the key assertion - HTTP_ROUTE should be captured
                            // This is expected to FAIL due to the bug
                            equalTo(HTTP_ROUTE, "/secured/test"))));
  }
}
