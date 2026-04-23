/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.oshi;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import org.junit.jupiter.api.Test;

public abstract class AbstractSystemMetricsTest {

  protected abstract void registerMetrics();

  protected abstract InstrumentationExtension testing();

  @Test
  void test() {
    // when
    registerMetrics();

    // then
    testing()
        .waitAndAssertMetrics(
            "io.opentelemetry.oshi",
            metric ->
                metric
                    .hasName("system.memory.usage")
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
                    .hasName("system.memory.utilization")
                    .hasUnit("1")
                    // TODO: Provide fuzzy value matching
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.satisfies(
                                g ->
                                    assertThat(g.getPoints())
                                        .anySatisfy(
                                            point -> assertThat(point.getValue()).isPositive()))));
    testing()
        .waitAndAssertMetrics(
            "io.opentelemetry.oshi",
            metric ->
                metric.hasName("system.network.io").hasUnit("By").hasLongSumSatisfying(sum -> {}));
    testing()
        .waitAndAssertMetrics(
            "io.opentelemetry.oshi",
            metric ->
                metric
                    .hasName("system.network.packets")
                    .hasUnit("{packets}")
                    .hasLongSumSatisfying(sum -> {}));
    testing()
        .waitAndAssertMetrics(
            "io.opentelemetry.oshi",
            metric ->
                metric
                    .hasName("system.network.errors")
                    .hasUnit("{errors}")
                    .hasLongSumSatisfying(sum -> {}));
    testing()
        .waitAndAssertMetrics(
            "io.opentelemetry.oshi",
            metric ->
                metric.hasName("system.disk.io").hasUnit("By").hasLongSumSatisfying(sum -> {}));
    testing()
        .waitAndAssertMetrics(
            "io.opentelemetry.oshi",
            metric ->
                metric
                    .hasName("system.disk.operations")
                    .hasUnit("{operations}")
                    .hasLongSumSatisfying(sum -> {}));
  }
}
