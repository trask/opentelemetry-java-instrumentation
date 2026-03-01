# [Semconv] Dual Semconv Testing

This document covers how to write tests that correctly handle dual semantic convention (semconv)
support — emitting both old (incubating) and new (stable) attributes during the migration period
that ends when old semconv is removed in 3.0.

---

## Background: The Three Modes

The system property `otel.semconv-stability.opt-in` (or env `OTEL_SEMCONV_STABILITY_OPT_IN`)
controls which attributes are emitted at runtime. Tests must run in all applicable modes.

| Property value | Old attrs emitted | Stable attrs emitted | Purpose |
|---------------|:-----------------:|:--------------------:|---------|
| *(unset)* | ✅ | ❌ | Default / legacy mode — what most users run today |
| `database` | ❌ | ✅ | Stable-only — users who have opted in |
| `database/dup` | ✅ | ✅ | Both — migration period support |

Multiple domains can be comma-separated: `database,code,service.peer`.

Available domains and their `SemconvStability` methods:

| Domain | `opt-in` value | Methods |
|--------|---------------|---------|
| Database | `database` / `database/dup` | `emitOldDatabaseSemconv()`, `emitStableDatabaseSemconv()` |
| Code | `code` / `code/dup` | `emitOldCodeSemconv()`, `emitStableCodeSemconv()` |
| RPC | `rpc` / `rpc/dup` | `emitOldRpcSemconv()`, `emitStableRpcSemconv()` |
| Service peer | `service.peer` / `service.peer/dup` | `emitOldServicePeerSemconv()`, `emitStableServicePeerSemconv()` |

All methods are in `io.opentelemetry.instrumentation.api.internal.SemconvStability` and must be
**statically imported** — never called as `SemconvStability.emitStable*()`.

---

## Gradle Test Task Setup

Every instrumentation module that has semconv versioning **must** define separate test tasks for
each mode and wire all of them into `check`. The domain-specific parts are the `jvmArgs` and
`metadataConfig` values — for the full explanation of `testClassesDirs`, `classpath`,
`collectMetadata`, and `metadataConfig` requirements, see
[testing-gradle-tasks.md](testing-gradle-tasks.md).

Database domain example (stable-only task):

```kotlin
val testStableSemconv by registering(Test::class) {
  // testClassesDirs and classpath required — see testing-gradle-tasks.md
  testClassesDirs = sourceSets.test.get().output.classesDirs
  classpath = sourceSets.test.get().runtimeClasspath
  jvmArgs("-Dotel.semconv-stability.opt-in=database")
  systemProperty("metadataConfig", "otel.semconv-stability.opt-in=database")
}
```

Code domain example (stable + both tasks):

```kotlin
val testStableSemconv by registering(Test::class) {
  testClassesDirs = sourceSets.test.get().output.classesDirs
  classpath = sourceSets.test.get().runtimeClasspath
  jvmArgs("-Dotel.semconv-stability.opt-in=code")
  systemProperty("metadataConfig", "otel.semconv-stability.opt-in=code")
}

val testBothSemconv by registering(Test::class) {
  testClassesDirs = sourceSets.test.get().output.classesDirs
  classpath = sourceSets.test.get().runtimeClasspath
  jvmArgs("-Dotel.semconv-stability.opt-in=code/dup")
  systemProperty("metadataConfig", "otel.semconv-stability.opt-in=code/dup")
}
```

Both tasks must be wired into `check { dependsOn(testStableSemconv, testBothSemconv) }`.

---

## Asserting Attributes in Tests

### Pattern 1: `if (emitStable*()) / if (emitOld*())`

Use when the assertion structure changes significantly between modes (e.g. attribute name changes):

```java
import static io.opentelemetry.instrumentation.api.internal.SemconvStability.emitOldCodeSemconv;
import static io.opentelemetry.instrumentation.api.internal.SemconvStability.emitStableCodeSemconv;
import static io.opentelemetry.semconv.CodeAttributes.CODE_FUNCTION_NAME;
import static io.opentelemetry.semconv.incubating.CodeIncubatingAttributes.CODE_FUNCTION;
import static io.opentelemetry.semconv.incubating.CodeIncubatingAttributes.CODE_NAMESPACE;

@SuppressWarnings("deprecation") // testing deprecated semconv
@Test
void someTest() {
  // ...
  if (emitStableCodeSemconv()) {
    assertThat(attributes).containsEntry(CODE_FUNCTION_NAME, "MyClass.myMethod");
  }
  if (emitOldCodeSemconv()) {
    assertThat(attributes)
        .containsEntry(CODE_NAMESPACE, "MyClass")
        .containsEntry(CODE_FUNCTION, "myMethod");
  }
}
```

Use `if` (not `if/else`) so that in `dup` mode (`/dup`) both branches execute.

### Pattern 2: `maybeStable()` — attribute key substitution

Use for database attributes where only the key name changes and the value stays the same:

```java
import static io.opentelemetry.instrumentation.testing.junit.db.SemconvStabilityUtil.maybeStable;
import static io.opentelemetry.semconv.incubating.DbIncubatingAttributes.DB_STATEMENT;

@SuppressWarnings("deprecation") // testing deprecated semconv
void checkSpan(SpanDataAssert span) {
  span.hasAttribute(equalTo(maybeStable(DB_STATEMENT), "SELECT ?"));
}
```

`maybeStable(oldKey)` returns the stable equivalent key when `emitStableDatabaseSemconv()` is
true, or the old key when in legacy mode. It does NOT cover `dup` mode — use `if` blocks if you
need to assert both keys simultaneously.

**Available mappings in `SemconvStabilityUtil`:**

| Old key | Stable key |
|---------|-----------|
| `DB_NAME` | `DB_NAMESPACE` |
| `DB_STATEMENT` | `DB_QUERY_TEXT` |
| `DB_OPERATION` | `DB_OPERATION_NAME` |
| `DB_SQL_TABLE` | `DB_COLLECTION_NAME` |
| `DB_CASSANDRA_TABLE` | `DB_COLLECTION_NAME` |
| `DB_MONGODB_COLLECTION` | `DB_COLLECTION_NAME` |
| `DB_SYSTEM` | `DB_SYSTEM_NAME` |
| Cassandra-specific | See `SemconvStabilityUtil.buildMap()` |

Also available:
- `SemconvStabilityUtil.maybeStableDbSystemName(String oldName)` — maps old DB system name strings
  to their stable equivalents (e.g. `"cosmosdb"` → `"azure.cosmosdb"`)
- `SemconvServiceStabilityUtil.maybeStablePeerService()` — returns `SERVICE_PEER_NAME` or
  `PEER_SERVICE` based on `service.peer` opt-in
- `SemconvCodeStabilityUtil` — helper methods for `code.*` attributes:
  - `codeFunctionAssertions(Class<?>, String)` — returns `List<AttributeAssertion>` covering both
    old (`CODE_NAMESPACE` + `CODE_FUNCTION`) and new (`CODE_FUNCTION_NAME`) based on mode
  - `codeFileAndLineAssertions(String)` — covers `CODE_FILEPATH`/`CODE_FILE_PATH` and
    `CODE_LINENO`/`CODE_LINE_NUMBER`

### Pattern 3: Inline ternary for span names

When the span name itself changes between stable and old:

```java
span.hasName(emitStableDatabaseSemconv() ? "SELECT" : "SELECT dbname")
```

### Pattern 4: `assumeTrue` to skip a test in the wrong mode

Use when a test is only meaningful in one mode:

```java
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static io.opentelemetry.instrumentation.api.internal.SemconvStability.emitStableDatabaseSemconv;

@Test
void testErrorTypeAttribute() {
  assumeTrue(emitStableDatabaseSemconv());
  // test only makes sense with stable semconv (ERROR_TYPE replaces DB_RESPONSE_STATUS_CODE)
  // ...
}
```

---

## Class-Level Annotation

Always suppress deprecation warnings at the class level when the test exercises old semconv:

```java
@SuppressWarnings("deprecation") // using deprecated semconv
class MyDbTest {
  // ...
}
```

Add a comment explaining what is deprecated and why it is intentional.

---

## DB Semconv Migration Specifics

`DB_RESPONSE_STATUS_CODE` was removed from stable DB semconv and replaced by `ERROR_TYPE`
from `io.opentelemetry.semconv.ErrorAttributes`:

```java
// ❌ old — only exists in old semconv
if (emitOldDatabaseSemconv()) {
  span.hasAttribute(equalTo(DB_RESPONSE_STATUS_CODE, "42"));
}

// ✅ stable — use ERROR_TYPE
if (emitStableDatabaseSemconv()) {
  span.hasAttribute(equalTo(ERROR_TYPE, "42"));
}
```

Do NOT use `maybeStable()` for this — the old and stable keys serve different semantic purposes
and do not have a 1:1 mapping in `SemconvStabilityUtil`.

---

## Import Checklist

```java
// SemconvStability methods — always static import, never qualified
import static io.opentelemetry.instrumentation.api.internal.SemconvStability.emitOldDatabaseSemconv;
import static io.opentelemetry.instrumentation.api.internal.SemconvStability.emitStableDatabaseSemconv;
import static io.opentelemetry.instrumentation.api.internal.SemconvStability.emitOldCodeSemconv;
import static io.opentelemetry.instrumentation.api.internal.SemconvStability.emitStableCodeSemconv;
import static io.opentelemetry.instrumentation.api.internal.SemconvStability.emitOldRpcSemconv;
import static io.opentelemetry.instrumentation.api.internal.SemconvStability.emitStableRpcSemconv;
import static io.opentelemetry.instrumentation.api.internal.SemconvStability.emitOldServicePeerSemconv;
import static io.opentelemetry.instrumentation.api.internal.SemconvStability.emitStableServicePeerSemconv;

// DB test helpers
import static io.opentelemetry.instrumentation.testing.junit.db.SemconvStabilityUtil.maybeStable;
import static io.opentelemetry.instrumentation.testing.junit.db.SemconvStabilityUtil.maybeStableDbSystemName;

// Code test helpers
import io.opentelemetry.instrumentation.testing.junit.code.SemconvCodeStabilityUtil;

// Service peer test helper
import static io.opentelemetry.instrumentation.testing.junit.service.SemconvServiceStabilityUtil.maybeStablePeerService;

// Stable semconv attributes (from opentelemetry-semconv artifact, never use in library/ prod code)
import static io.opentelemetry.semconv.DbAttributes.*;
import static io.opentelemetry.semconv.ErrorAttributes.ERROR_TYPE;

// Old (incubating) semconv attributes
import static io.opentelemetry.semconv.incubating.DbIncubatingAttributes.*;
```

---

## Common Mistakes

| ❌ Wrong | ✅ Right |
|----------|---------|
| `SemconvStability.emitStableDatabaseSemconv()` | `emitStableDatabaseSemconv()` (static import) |
| `if/else` for dup-mode assertions | Two separate `if` blocks |
| `maybeStable(DB_RESPONSE_STATUS_CODE)` | Use `emitOld*()` / `emitStable*()` if blocks |
| Missing `testStableSemconv` task in `check` | Wire all mode tasks into `check` |
| Missing `testClassesDirs` + `classpath` in task | Always include both when registering a `Test` task |
| Asserting stable attribute without `@SuppressWarnings("deprecation")` when using incubating imports | Add it at class level with explanatory comment |
