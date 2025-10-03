/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.jdbc;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.jdbc.internal.DbRequest;
import io.opentelemetry.instrumentation.jdbc.internal.OpenTelemetryConnection;
import io.opentelemetry.instrumentation.jdbc.internal.dbinfo.DbInfo;
import java.sql.Connection;

/** Entrypoint for instrumenting JDBC Connections. */
public final class JdbcTelemetry {

  /** Returns a new {@link JdbcTelemetry} configured with the given {@link OpenTelemetry}. */
  public static JdbcTelemetry create(OpenTelemetry openTelemetry) {
    return builder(openTelemetry).build();
  }

  /** Returns a new {@link JdbcTelemetryBuilder} configured with the given {@link OpenTelemetry}. */
  public static JdbcTelemetryBuilder builder(OpenTelemetry openTelemetry) {
    return new JdbcTelemetryBuilder(openTelemetry);
  }

  private final Instrumenter<DbRequest, Void> statementInstrumenter;
  private final Instrumenter<DbRequest, Void> transactionInstrumenter;
  private final boolean captureQueryParameters;

  JdbcTelemetry(
      Instrumenter<DbRequest, Void> statementInstrumenter,
      Instrumenter<DbRequest, Void> transactionInstrumenter,
      boolean captureQueryParameters) {
    this.statementInstrumenter = statementInstrumenter;
    this.transactionInstrumenter = transactionInstrumenter;
    this.captureQueryParameters = captureQueryParameters;
  }

  /**
   * Wraps a JDBC {@link Connection} to enable OpenTelemetry instrumentation for statements and
   * transactions.
   */
  public Connection wrap(Connection connection) {
    DbInfo dbInfo =
        io.opentelemetry.instrumentation.jdbc.internal.JdbcUtils.computeDbInfo(connection);
    return OpenTelemetryConnection.create(
        connection, dbInfo, statementInstrumenter, transactionInstrumenter, captureQueryParameters);
  }
}
