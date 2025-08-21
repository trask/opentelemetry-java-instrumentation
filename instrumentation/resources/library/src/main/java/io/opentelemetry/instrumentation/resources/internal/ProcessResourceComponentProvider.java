/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.resources.internal;

import io.opentelemetry.instrumentation.resources.ProcessResource;
import io.opentelemetry.instrumentation.resources.ProcessRuntimeResource;

/**
 * Declarative config process resource provider.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class ProcessResourceComponentProvider extends ResourceComponentProvider {
  public ProcessResourceComponentProvider() {
    super("process", () -> ProcessResource.get().merge(ProcessRuntimeResource.get()));
  }
}
