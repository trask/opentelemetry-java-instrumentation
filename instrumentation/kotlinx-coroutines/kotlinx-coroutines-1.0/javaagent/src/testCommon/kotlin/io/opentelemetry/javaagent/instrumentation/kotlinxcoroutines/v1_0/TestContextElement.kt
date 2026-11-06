/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.kotlinxcoroutines.v1_0

import io.opentelemetry.context.Context
import io.opentelemetry.context.Scope
import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.CoroutineContext

class TestContextElement(private val otelContext: Context) : ThreadContextElement<Scope> {
  companion object Key : CoroutineContext.Key<TestContextElement> {
  }

  override val key: CoroutineContext.Key<TestContextElement>
    get() = Key

  override fun restoreThreadContext(context: CoroutineContext, oldState: Scope) {
    oldState.close()
  }

  override fun updateThreadContext(context: CoroutineContext): Scope = otelContext.makeCurrent()
}
