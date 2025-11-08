/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.tooling.util;

import io.opentelemetry.javaagent.tooling.util.ClassLoaderMap.Injector;
import java.util.function.Supplier;

/**
 * Associate value with a class loader. Added value will behave as if it was stored in a field in
 * the class loader object, meaning that the value can be garbage collected once the class loader is
 * garbage collected and referencing the class loader from the value will not prevent garbage
 * collector from collecting the class loader.
 */
public final class ClassLoaderValue<T> {

  private final Injector classInjector;

  public ClassLoaderValue() {
    this(ClassLoaderMap.defaultInjector);
  }

  // visible for testing
  ClassLoaderValue(Injector classInjector) {
    this.classInjector = classInjector;
  }

  public T get(ClassLoader classLoader) {
    @SuppressWarnings("unchecked")
    T result = (T) ClassLoaderMap.get(classLoader, classInjector, this);
    return result;
  }

  public void put(ClassLoader classLoader, T value) {
    ClassLoaderMap.put(classLoader, classInjector, this, value);
  }

  public T computeIfAbsent(ClassLoader classLoader, Supplier<? extends T> value) {
    @SuppressWarnings("unchecked")
    T result = (T) ClassLoaderMap.computeIfAbsent(classLoader, classInjector, this, value);
    return result;
  }
}
