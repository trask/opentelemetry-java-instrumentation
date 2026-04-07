/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.jsf.javax;

import io.opentelemetry.instrumentation.api.instrumenter.ErrorCauseExtractor;
import javax.faces.FacesException;

public class JsfErrorCauseExtractor implements ErrorCauseExtractor {

  @Override
  public Throwable extract(Throwable t) {
    while (t.getCause() != null && t instanceof FacesException) {
      t = t.getCause();
    }
    return ErrorCauseExtractor.getDefault().extract(t);
  }
}
