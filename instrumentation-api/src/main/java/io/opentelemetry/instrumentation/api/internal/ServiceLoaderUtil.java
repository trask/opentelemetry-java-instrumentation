/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.internal;

import java.util.ServiceLoader;
import java.util.function.Function;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class ServiceLoaderUtil {

  private static volatile Function<Class<?>, Iterable<?>> loadFunction = ServiceLoader::load;

  private ServiceLoaderUtil() {}

  public static <T> Iterable<T> load(Class<T> clazz) {
    @SuppressWarnings("unchecked")
    Iterable<T> result = (Iterable<T>) loadFunction.apply(clazz);
    return result;
  }

  public static void setLoadFunction(Function<Class<?>, Iterable<?>> customLoadFunction) {
    loadFunction = customLoadFunction;
  }
}
