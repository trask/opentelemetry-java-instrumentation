/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.opentelemetryapi.v1_52.incubator.logs;

import application.io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import application.io.opentelemetry.api.incubator.logs.ExtendedLogger;
import io.opentelemetry.javaagent.instrumentation.opentelemetryapi.v1_27.logs.ApplicationLogger;

class ApplicationLogger152Incubator extends ApplicationLogger implements ExtendedLogger {

  private final io.opentelemetry.api.logs.Logger agentLogger;

  ApplicationLogger152Incubator(io.opentelemetry.api.logs.Logger agentLogger) {
    super(agentLogger);
    this.agentLogger = agentLogger;
  }

  @Override
  public boolean isEnabled() {
    return ((io.opentelemetry.api.incubator.logs.ExtendedLogger) agentLogger).isEnabled();
  }

  @Override
  public ExtendedLogRecordBuilder logRecordBuilder() {
    return new ApplicationLogRecordBuilder152Incubator(agentLogger.logRecordBuilder());
  }
}
