/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.alibabadruid.v1_0;

import com.alibaba.druid.pool.DruidDataSourceMBean;
import io.opentelemetry.api.OpenTelemetry;

/** Entrypoint for instrumenting Alibaba Druid database connection pools. */
public final class DruidTelemetry {
  private final OpenTelemetry openTelemetry;

  /** Returns a new {@link DruidTelemetry} configured with the given {@link OpenTelemetry}. */
  public static DruidTelemetry create(OpenTelemetry openTelemetry) {
    return new DruidTelemetry(openTelemetry);
  }

  private DruidTelemetry(OpenTelemetry openTelemetry) {
    this.openTelemetry = openTelemetry;
  }

  /** Start collecting metrics for given connection pool. */
  public void registerMetrics(DruidDataSourceMBean dataSource, String dataSourceName) {
    ConnectionPoolMetrics.registerMetrics(openTelemetry, dataSource, dataSourceName);
  }

  /** Stop collecting metrics for given connection pool. */
  public void unregisterMetrics(DruidDataSourceMBean dataSource) {
    ConnectionPoolMetrics.unregisterMetrics(dataSource);
  }
}
