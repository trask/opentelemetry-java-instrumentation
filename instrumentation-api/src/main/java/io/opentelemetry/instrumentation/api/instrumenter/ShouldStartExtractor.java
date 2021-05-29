/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter;

import io.opentelemetry.context.Context;

@FunctionalInterface
public interface ShouldStartExtractor<REQUEST> {

  boolean shouldStart(Context parentContext, REQUEST request);
}
