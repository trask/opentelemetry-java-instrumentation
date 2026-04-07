/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.myfaces.v3_0;

import io.opentelemetry.javaagent.instrumentation.jsf.jakarta.JsfErrorCauseExtractor;
import jakarta.el.ELException;

public class MyFacesErrorCauseExtractor extends JsfErrorCauseExtractor {

  @Override
  public Throwable extract(Throwable t) {
    t = super.extract(t);
    while (t.getCause() != null && t instanceof ELException) {
      t = t.getCause();
    }
    return t;
  }
}
