/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.rocketmqclient.v5_0;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.concurrent.CompletableFuture;

public class CompletableFutureWrapper {

  private CompletableFutureWrapper() {}

  public static <T> CompletableFuture<T> wrap(CompletableFuture<T> future) {
    CompletableFuture<T> result = new CompletableFuture<>();
    Context context = Context.current();
    future.whenComplete(
        (T value, Throwable t) -> {
          try (Scope ignored = context.makeCurrent()) {
            if (t != null) {
              result.completeExceptionally(t);
            } else {
              result.complete(value);
            }
          }
        });

    return result;
  }
}
