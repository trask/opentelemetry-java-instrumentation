/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.jdbc.datasource;

import static io.opentelemetry.instrumentation.jdbc.datasource.internal.JdbcDataSourceInstrumenterFactory.createDataSourceInstrumenter;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.javaagent.bootstrap.jdbc.DbInfo;
import javax.sql.DataSource;

public final class JdbcDataSourceSingletons {

  @SuppressWarnings("unchecked")
  private static final Instrumenter<DataSource, DbInfo> DATASOURCE_INSTRUMENTER =
      (Instrumenter<DataSource, DbInfo>) createDataSourceInstrumenter(GlobalOpenTelemetry.get(), true);

  public static Instrumenter<DataSource, DbInfo> dataSourceInstrumenter() {
    return DATASOURCE_INSTRUMENTER;
  }

  private JdbcDataSourceSingletons() {}
}
