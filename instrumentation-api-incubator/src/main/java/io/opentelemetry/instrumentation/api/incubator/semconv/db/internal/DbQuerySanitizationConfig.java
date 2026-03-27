/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.incubator.semconv.db.internal;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.instrumentation.api.incubator.config.internal.DeclarativeConfigUtil;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class DbQuerySanitizationConfig {
  private static final Logger logger = Logger.getLogger(DbQuerySanitizationConfig.class.getName());

  public static boolean getCommonQuerySanitizationEnabled(OpenTelemetry openTelemetry) {
    return getCommonQuerySanitizationEnabled(openTelemetry, true);
  }

  public static boolean getCommonQuerySanitizationEnabled(
      OpenTelemetry openTelemetry, boolean defaultValue) {
    Boolean querySanitizationEnabled =
        getBooleanWithDeprecatedAlias(
            DeclarativeConfigUtil.getInstrumentationConfig(openTelemetry, "common")
                .get("database"));
    return querySanitizationEnabled != null ? querySanitizationEnabled : defaultValue;
  }

  public static boolean getQuerySanitizationEnabled(
      OpenTelemetry openTelemetry, String instrumentationName) {
    Boolean querySanitizationEnabled =
        getBooleanWithDeprecatedAlias(
            DeclarativeConfigUtil.getInstrumentationConfig(openTelemetry, instrumentationName));
    if (querySanitizationEnabled != null) {
      return querySanitizationEnabled;
    }
    return getCommonQuerySanitizationEnabled(openTelemetry);
  }

  @Nullable
  private static Boolean getBooleanWithDeprecatedAlias(DeclarativeConfigProperties config) {
    Boolean value = config.get("query_sanitization").getBoolean("enabled");
    Boolean deprecatedValue = config.get("statement_sanitizer").getBoolean("enabled");
    if (deprecatedValue != null) {
      logger.warning(
          "statement_sanitizer is deprecated in declarative configuration"
              + " and has been replaced by query_sanitization.");
    }
    return value != null ? value : deprecatedValue;
  }

  private DbQuerySanitizationConfig() {}
}
