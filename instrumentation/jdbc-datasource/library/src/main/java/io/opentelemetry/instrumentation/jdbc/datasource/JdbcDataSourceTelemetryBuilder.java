/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.jdbc.datasource;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.jdbc.datasource.internal.JdbcDataSourceInstrumenterFactory;
import io.opentelemetry.instrumentation.jdbc.internal.dbinfo.DbInfo;
import javax.sql.DataSource;

/** A builder of {@link JdbcDataSourceTelemetry}. */
public final class JdbcDataSourceTelemetryBuilder {

  private final OpenTelemetry openTelemetry;

  JdbcDataSourceTelemetryBuilder(OpenTelemetry openTelemetry) {
    this.openTelemetry = openTelemetry;
  }

  /**
   * Returns a new {@link JdbcDataSourceTelemetry} with the settings of this {@link
   * JdbcDataSourceTelemetryBuilder}.
   */
  @SuppressWarnings("unchecked")
  public JdbcDataSourceTelemetry build() {
    Instrumenter<DataSource, DbInfo> dataSourceInstrumenter =
        (Instrumenter<DataSource, DbInfo>) JdbcDataSourceInstrumenterFactory.createDataSourceInstrumenter(openTelemetry, true);

    return new JdbcDataSourceTelemetry(dataSourceInstrumenter);
  }
}
