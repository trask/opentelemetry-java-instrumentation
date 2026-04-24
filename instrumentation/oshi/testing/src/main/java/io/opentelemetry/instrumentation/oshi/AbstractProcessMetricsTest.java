/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.oshi;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public abstract class AbstractProcessMetricsTest {

  protected abstract void registerMetrics();

  protected abstract InstrumentationExtension testing();

  @Test
  @EnabledIfSystemProperty(named = "testExperimental", matches = "true")
  void test() {
    // when
    registerMetrics();

    // then
    testing()
        .waitAndAssertMetrics(
            "io.opentelemetry.oshi",
            metric ->
                metric
                    .hasName("runtime.java.memory")
                    .hasUnit("By")
                    // TODO: Provide fuzzy value matching
                    .hasLongSumSatisfying(
                        sum ->
                            sum.satisfies(
                                s ->
                                    assertThat(s.getPoints())
                                        .anySatisfy(
                                            point -> assertThat(point.getValue()).isPositive()))));
    testing()
        .waitAndAssertMetrics(
            "io.opentelemetry.oshi",
            metric ->
                metric
                    .hasName("runtime.java.cpu_time")
                    .hasUnit("ms")
                    // TODO: Provide fuzzy value matching
                    .hasLongGaugeSatisfying(
                        gauge ->
                            gauge.satisfies(
                                g ->
                                    assertThat(g.getPoints())
                                        .anySatisfy(
                                            point -> assertThat(point.getValue()).isPositive()))));
  }
}
