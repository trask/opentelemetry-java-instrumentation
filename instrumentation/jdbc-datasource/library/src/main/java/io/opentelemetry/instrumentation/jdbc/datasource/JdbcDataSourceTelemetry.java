/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.jdbc.datasource;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.jdbc.internal.dbinfo.DbInfo;
import javax.sql.DataSource;

/** Entrypoint for instrumenting a JDBC DataSources. */
public final class JdbcDataSourceTelemetry {

  /**
   * Returns a new {@link JdbcDataSourceTelemetry} configured with the given {@link OpenTelemetry}.
   */
  public static JdbcDataSourceTelemetry create(OpenTelemetry openTelemetry) {
    return builder(openTelemetry).build();
  }

  /**
   * Returns a new {@link JdbcDataSourceTelemetryBuilder} configured with the given {@link
   * OpenTelemetry}.
   */
  public static JdbcDataSourceTelemetryBuilder builder(OpenTelemetry openTelemetry) {
    return new JdbcDataSourceTelemetryBuilder(openTelemetry);
  }

  private final Instrumenter<DataSource, DbInfo> dataSourceInstrumenter;

  JdbcDataSourceTelemetry(Instrumenter<DataSource, DbInfo> dataSourceInstrumenter) {
    this.dataSourceInstrumenter = dataSourceInstrumenter;
  }

  public DataSource wrap(DataSource dataSource) {
    return new OpenTelemetryDataSource(dataSource, this.dataSourceInstrumenter);
  }
}
