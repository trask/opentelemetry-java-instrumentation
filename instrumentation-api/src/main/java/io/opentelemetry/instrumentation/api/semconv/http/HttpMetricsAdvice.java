/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.semconv.http;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.logging.Level.FINE;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.semconv.ErrorAttributes;
import io.opentelemetry.semconv.HttpAttributes;
import io.opentelemetry.semconv.NetworkAttributes;
import io.opentelemetry.semconv.ServerAttributes;
import io.opentelemetry.semconv.UrlAttributes;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nullable;

final class HttpMetricsAdvice {

  private static final Logger logger = Logger.getLogger(HttpMetricsAdvice.class.getName());

  @Nullable
  private static final Class<?> extendedDoubleHistogramBuilderClass =
      getExtendedDoubleHistogramBuilderClass();

  @Nullable private static final Method setAttributesAdviceMethod = getSetAttributesAdviceMethod();

  static final List<Double> DURATION_SECONDS_BUCKETS =
      unmodifiableList(
          asList(0.005, 0.01, 0.025, 0.05, 0.075, 0.1, 0.25, 0.5, 0.75, 1.0, 2.5, 5.0, 7.5, 10.0));

  static void applyClientDurationAdvice(DoubleHistogramBuilder builder) {
    invoke(
        builder,
        HttpAttributes.HTTP_REQUEST_METHOD,
        HttpAttributes.HTTP_RESPONSE_STATUS_CODE,
        ErrorAttributes.ERROR_TYPE,
        NetworkAttributes.NETWORK_PROTOCOL_NAME,
        NetworkAttributes.NETWORK_PROTOCOL_VERSION,
        ServerAttributes.SERVER_ADDRESS,
        ServerAttributes.SERVER_PORT);
  }

  static void applyServerDurationAdvice(DoubleHistogramBuilder builder) {
    invoke(
        builder,
        HttpAttributes.HTTP_ROUTE,
        HttpAttributes.HTTP_REQUEST_METHOD,
        HttpAttributes.HTTP_RESPONSE_STATUS_CODE,
        ErrorAttributes.ERROR_TYPE,
        NetworkAttributes.NETWORK_PROTOCOL_NAME,
        NetworkAttributes.NETWORK_PROTOCOL_VERSION,
        UrlAttributes.URL_SCHEME);
  }

  private static void invoke(DoubleHistogramBuilder builder, AttributeKey<?>... attributes) {
    if (extendedDoubleHistogramBuilderClass == null || setAttributesAdviceMethod == null) {
      return;
    }
    if (!extendedDoubleHistogramBuilderClass.isInstance(builder)) {
      return;
    }
    try {
      setAttributesAdviceMethod.invoke(builder, asList(attributes));
    } catch (IllegalAccessException | InvocationTargetException e) {
      logger.log(FINE, e.getMessage(), e);
    }
  }

  private static Class<?> getExtendedDoubleHistogramBuilderClass() {
    try {
      return Class.forName("io.opentelemetry.api.incubator.metrics.ExtendedDoubleHistogramBuilder");
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private static Method getSetAttributesAdviceMethod() {
    Class<?> extendedDoubleHistogramBuilderClass = getExtendedDoubleHistogramBuilderClass();
    if (extendedDoubleHistogramBuilderClass == null) {
      return null;
    }
    try {
      return extendedDoubleHistogramBuilderClass.getMethod("setAttributesAdvice", List.class);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  private HttpMetricsAdvice() {}
}
