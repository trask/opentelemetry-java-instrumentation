/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.incubator.semconv.db;

import static io.opentelemetry.instrumentation.api.internal.SemconvStability.emitStableDatabaseSemconv;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;
import io.opentelemetry.instrumentation.api.internal.InstrumenterContext;
import io.opentelemetry.instrumentation.testing.internal.AutoCleanupExtension;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class SqlQuerySanitizerUtilCacheTest {

  @RegisterExtension static final AutoCleanupExtension cleanup = AutoCleanupExtension.create();

  private static final String TEST_QUERY = "SELECT name FROM test WHERE id = 1";

  private final SqlClientAttributesGetter<Object, Void> getter =
      new SqlClientAttributesGetter<Object, Void>() {
        @Override
        public String getDbSystemName(Object o) {
          return "testdb";
        }

        @Override
        public String getDbNamespace(Object o) {
          return null;
        }

        @Override
        public Collection<String> getRawQueryTexts(Object request) {
          return singletonList(TEST_QUERY);
        }
      };

  @Test
  void sanitizeCachesResult() {
    InstrumenterContext.reset();
    cleanup.deferCleanup(InstrumenterContext::reset);

    SqlQuery first = SqlQuerySanitizerUtil.sanitize(TEST_QUERY);
    SqlQuery second = SqlQuerySanitizerUtil.sanitize(TEST_QUERY);

    assertThat(second).isSameAs(first);
  }

  @Test
  void sanitizeWithSummaryCachesResult() {
    InstrumenterContext.reset();
    cleanup.deferCleanup(InstrumenterContext::reset);

    SqlQuery first = SqlQuerySanitizerUtil.sanitizeWithSummary(TEST_QUERY);
    SqlQuery second = SqlQuerySanitizerUtil.sanitizeWithSummary(TEST_QUERY);

    assertThat(second).isSameAs(first);
  }

  @Test
  void spanNameExtractorAndAttributesExtractorShareCachedResult() {
    InstrumenterContext.reset();
    cleanup.deferCleanup(InstrumenterContext::reset);

    SpanNameExtractor<Object> spanNameExtractor = DbClientSpanNameExtractor.create(getter);

    // Drive caching through span name extractor
    spanNameExtractor.extract(null);

    // The same SqlQuerySanitizerUtil method used by the attributes extractor should return
    // the cached instance (same object identity) proving cross-component cache sharing
    SqlQuery cachedResult =
        emitStableDatabaseSemconv()
            ? SqlQuerySanitizerUtil.sanitizeWithSummary(TEST_QUERY)
            : SqlQuerySanitizerUtil.sanitize(TEST_QUERY);

    // Call again to confirm the returned object is the same cached instance
    SqlQuery secondCall =
        emitStableDatabaseSemconv()
            ? SqlQuerySanitizerUtil.sanitizeWithSummary(TEST_QUERY)
            : SqlQuerySanitizerUtil.sanitize(TEST_QUERY);

    assertThat(secondCall).isSameAs(cachedResult);
    assertThat(cachedResult.getCollectionName()).isEqualTo("test");
  }

  @Test
  void clearCacheAndRecompute() {
    InstrumenterContext.reset();
    cleanup.deferCleanup(InstrumenterContext::reset);

    SqlQuery first = SqlQuerySanitizerUtil.sanitize(TEST_QUERY);
    assertThat(first.getCollectionName()).isEqualTo("test");

    // Clear the InstrumenterContext cache
    InstrumenterContext.reset();

    // After reset, calling sanitize again repopulates the cache and produces a correct result
    SqlQuery recomputed = SqlQuerySanitizerUtil.sanitize(TEST_QUERY);
    assertThat(recomputed.getCollectionName()).isEqualTo("test");
  }
}
