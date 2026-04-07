/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.quartz.v2_0;

import io.opentelemetry.instrumentation.api.instrumenter.ErrorCauseExtractor;
import org.quartz.SchedulerException;

final class QuartzErrorCauseExtractor implements ErrorCauseExtractor {
  @Override
  public Throwable extract(Throwable t) {
    while (t instanceof SchedulerException
        && ((SchedulerException) t).getUnderlyingException() != null) {
      t = ((SchedulerException) t).getUnderlyingException();
    }
    return ErrorCauseExtractor.getDefault().extract(t);
  }
}
