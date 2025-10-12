/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.logback.appender.v1_0;

import static io.opentelemetry.instrumentation.testing.junit.code.SemconvCodeStabilityUtil.codeFileAndLineAssertions;
import static io.opentelemetry.instrumentation.testing.junit.code.SemconvCodeStabilityUtil.codeFunctionAssertions;

import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.LibraryInstrumentationExtension;
import io.opentelemetry.sdk.testing.assertj.AttributeAssertion;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

class LibraryLogbackTest extends AbstractLogbackTest {

  @RegisterExtension
  static final LibraryInstrumentationExtension testing = LibraryInstrumentationExtension.create();

  @BeforeEach
  void setup() {
    OpenTelemetryAppender.install(testing.getOpenTelemetry());
  }

  @Override
  protected InstrumentationExtension testing() {
    return testing;
  }

  @Override
  protected List<AttributeAssertion> addCodeLocationAttributes(String methodName) {
    List<AttributeAssertion> result = new ArrayList<>();
    result.addAll(codeFunctionAssertions(AbstractLogbackTest.class, methodName));
    result.addAll(codeFileAndLineAssertions("AbstractLogbackTest.java"));
    return result;
  }
}
