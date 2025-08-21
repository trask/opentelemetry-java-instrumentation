/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.resources.internal;

import io.opentelemetry.instrumentation.resources.HostIdResource;
import io.opentelemetry.instrumentation.resources.HostResource;
import io.opentelemetry.instrumentation.resources.OsResource;

/**
 * Declarative config host resource provider.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class HostResourceComponentProvider extends ResourceComponentProvider {
  public HostResourceComponentProvider() {
    super("host", () -> HostResource.get().merge(HostIdResource.get()).merge(OsResource.get()));
  }
}
