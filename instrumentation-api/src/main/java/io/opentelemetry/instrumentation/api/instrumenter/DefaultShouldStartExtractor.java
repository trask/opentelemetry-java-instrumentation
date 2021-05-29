/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.tracer.ClientSpan;
import io.opentelemetry.instrumentation.api.tracer.ServerSpan;

final class DefaultShouldStartExtractor<REQUEST> implements ShouldStartExtractor<REQUEST> {

  private final SpanKindExtractor<REQUEST> spanKindExtractor;

  static <REQUEST> ShouldStartExtractor<REQUEST> forSpanKindExtractor(
      SpanKindExtractor<REQUEST> spanKindExtractor) {
    return new DefaultShouldStartExtractor<>(spanKindExtractor);
  }

  DefaultShouldStartExtractor(SpanKindExtractor<REQUEST> spanKindExtractor) {
    this.spanKindExtractor = spanKindExtractor;
  }

  @Override
  public boolean shouldStart(Context parentContext, REQUEST request) {
    SpanKind spanKind = spanKindExtractor.extract(request);
    switch (spanKind) {
      case SERVER:
        return ServerSpan.fromContextOrNull(parentContext) == null;
      case CLIENT:
        return ClientSpan.fromContextOrNull(parentContext) == null;
      default:
        return true;
    }
  }
}
