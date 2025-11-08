/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.influxdb.v2_4;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class InfluxDbObjetWrapper {

  public static Object wrap(Object object, Context parentContext) {
    if (object instanceof Consumer) {
      @SuppressWarnings("unchecked")
      Consumer<Object> wrappedConsumer =
          (Consumer<Object>)
              o -> {
                try (Scope ignore = parentContext.makeCurrent()) {
                  ((Consumer<Object>) object).accept(o);
                }
              };
      return wrappedConsumer;
    } else if (object instanceof BiConsumer) {
      @SuppressWarnings("unchecked")
      BiConsumer<Object, Object> wrappedBiConsumer =
          (BiConsumer<Object, Object>)
              (o1, o2) -> {
                try (Scope ignore = parentContext.makeCurrent()) {
                  ((BiConsumer<Object, Object>) object).accept(o1, o2);
                }
              };
      return wrappedBiConsumer;
    } else if (object instanceof Runnable) {
      return (Runnable)
          () -> {
            try (Scope ignore = parentContext.makeCurrent()) {
              ((Runnable) object).run();
            }
          };
    }

    return object;
  }

  private InfluxDbObjetWrapper() {}
}
