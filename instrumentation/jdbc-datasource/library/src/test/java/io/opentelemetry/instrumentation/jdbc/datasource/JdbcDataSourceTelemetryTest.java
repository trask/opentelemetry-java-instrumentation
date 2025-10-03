/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.jdbc.datasource;

import static io.opentelemetry.instrumentation.api.internal.SemconvStability.emitStableDatabaseSemconv;
import static io.opentelemetry.instrumentation.testing.junit.db.SemconvStabilityUtil.maybeStable;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static io.opentelemetry.semconv.incubating.DbIncubatingAttributes.DB_CONNECTION_STRING;
import static io.opentelemetry.semconv.incubating.DbIncubatingAttributes.DB_NAME;
import static io.opentelemetry.semconv.incubating.DbIncubatingAttributes.DB_SYSTEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.LibraryInstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.code.SemconvCodeStabilityUtil;
import io.opentelemetry.sdk.testing.assertj.AttributeAssertion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("deprecation") // using deprecated semconv
class JdbcDataSourceTelemetryTest {

  @RegisterExtension
  static final InstrumentationExtension testing = LibraryInstrumentationExtension.create();

  @SuppressWarnings("deprecation") // using deprecated semconv
  @ParameterizedTest
  @MethodSource("getConnectionMethodsArguments")
  void shouldEmitGetConnectionSpans(GetConnectionFunction getConnection) throws SQLException {
    JdbcDataSourceTelemetry telemetry =
        JdbcDataSourceTelemetry.create(testing.getOpenTelemetry());
    DataSource dataSource = telemetry.wrap(new TestDataSource());

    testing.runWithSpan("parent", () -> getConnection.call(dataSource));

    List<AttributeAssertion> assertions =
        SemconvCodeStabilityUtil.codeFunctionAssertions(TestDataSource.class, "getConnection");
    assertions.add(equalTo(maybeStable(DB_SYSTEM), "postgresql"));
    assertions.add(equalTo(maybeStable(DB_NAME), "dbname"));
    assertions.add(
        equalTo(
            DB_CONNECTION_STRING,
            emitStableDatabaseSemconv() ? null : "postgresql://127.0.0.1:5432"));

    testing.waitAndAssertTraces(
        trace ->
            trace.hasSpansSatisfyingExactly(
                span -> span.hasName("parent"),
                span ->
                    span.hasName("TestDataSource.getConnection")
                        .hasKind(SpanKind.INTERNAL)
                        .hasParent(trace.getSpan(0))
                        .hasAttributesSatisfyingExactly(assertions)));
  }

  @ParameterizedTest
  @MethodSource("getConnectionMethodsArguments")
  void shouldNotEmitGetConnectionSpansWithoutParentSpan(GetConnectionFunction getConnection)
      throws SQLException {
    JdbcDataSourceTelemetry telemetry =
        JdbcDataSourceTelemetry.create(testing.getOpenTelemetry());
    DataSource dataSource = telemetry.wrap(new TestDataSource());

    getConnection.call(dataSource);

    assertThat(testing.waitForTraces(0)).isEmpty();
  }

  private static Stream<Arguments> getConnectionMethodsArguments() {
    GetConnectionFunction getConnection = DataSource::getConnection;
    GetConnectionFunction getConnectionWithUserAndPass = ds -> ds.getConnection(null, null);
    return Stream.of(arguments(getConnection), arguments(getConnectionWithUserAndPass));
  }

  @FunctionalInterface
  interface GetConnectionFunction {

    Connection call(DataSource dataSource) throws SQLException;
  }
}
