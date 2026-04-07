/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;

final class DefaultErrorCauseExtractor implements ErrorCauseExtractor {
  static final ErrorCauseExtractor INSTANCE = new DefaultErrorCauseExtractor();

  @Nullable
  private static final Class<?> COMPLETION_EXCEPTION_CLASS = getCompletionExceptionClass();

  @Override
  public Throwable extract(Throwable t) {
    if (t.getCause() != null
        && (t instanceof ExecutionException
            || isInstanceOfCompletionException(t)
            || t instanceof InvocationTargetException
            || t instanceof UndeclaredThrowableException)) {
      return extract(t.getCause());
    }
    return t;
  }

  private static boolean isInstanceOfCompletionException(Throwable t) {
    return COMPLETION_EXCEPTION_CLASS != null && COMPLETION_EXCEPTION_CLASS.isInstance(t);
  }

  @Nullable
  private static Class<?> getCompletionExceptionClass() {
    try {
      return Class.forName("java.util.concurrent.CompletionException");
    } catch (ClassNotFoundException ignored) {
      // Android level 21 does not support java.util.concurrent.CompletionException
      return null;
    }
  }

  private DefaultErrorCauseExtractor() {}
}
