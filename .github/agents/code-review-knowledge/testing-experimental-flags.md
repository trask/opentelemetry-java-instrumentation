# [Testing] Experimental Feature Flag Tests

## What `testExperimental` Is For

Some instrumentation modules support extra attributes that are disabled by default because they
are experimental (subject to change) or carry a performance cost. These attributes are gated
behind a JVM property such as:

```
otel.instrumentation.<module>.experimental-span-attributes=true
```

The `testExperimental` Gradle task runs the test suite with this flag enabled so that the
experimental attribute assertions are exercised in CI.

---

## Gradle Task Setup

The `testExperimental` task follows the standard custom test task pattern — see
[testing-gradle-tasks.md](testing-gradle-tasks.md) for the full explanation of `testClassesDirs`,
`classpath`, `collectMetadata`, and `metadataConfig` requirements.

The domain-specific parts are the `jvmArgs` and `metadataConfig` values:

```kotlin
val testExperimental by registering(Test::class) {
  // testClassesDirs and classpath required — see testing-gradle-tasks.md
  testClassesDirs = sourceSets.test.get().output.classesDirs
  classpath = sourceSets.test.get().runtimeClasspath
  jvmArgs("-Dotel.instrumentation.<module>.experimental-span-attributes=true")
  systemProperty("metadataConfig", "otel.instrumentation.<module>.experimental-span-attributes=true")
}
```

The task must be wired into `check { dependsOn(testExperimental) }`.

---

## Java Test Patterns

### Pattern 1: Inline ternary with a class-level constant

The simplest pattern. Read the flag once into a `private static final boolean` at the top of
the test class, then use an inline ternary in each assertion:

```java
private static final boolean EXPERIMENTAL_ATTRIBUTES =
    Boolean.getBoolean("otel.instrumentation.<module>.experimental-span-attributes");
```

Use the constant inline in span assertions — a `null` expected value asserts the attribute is
absent:

```java
span.hasAttributesSatisfyingExactly(
    equalTo(SpanAttributes.SOME_ATTRIBUTE, "value"),
    equalTo(ExperimentalAttributes.SOME_EXPERIMENTAL_ATTRIBUTE,
        EXPERIMENTAL_ATTRIBUTES ? "experimentalValue" : null));

// boolean attribute
equalTo(ExperimentalAttributes.SOME_FLAG, EXPERIMENTAL_ATTRIBUTES ? true : null)
```

### Pattern 2: `experimental()` helper method

When many assertions share the same flag, extract a small private (or package-private) helper
method to make assertions more readable. The generic single-type form:

```java
@Nullable
private static <T> T experimental(T value) {
  return EXPERIMENTAL_ATTRIBUTES_ENABLED ? value : null;
}
```

Usage makes the intent clear at the call site:

```java
assertions.add(equalTo(stringKey("job.system"), experimental("elasticjob")));
assertions.add(equalTo(longKey("sharding.item.index"), experimental(item)));
```

For modules with many tests spread across files, move the helper into a dedicated package-private
class (e.g. `ExperimentalTestHelper`) and static-import it:

```java
// ExperimentalTestHelper.java
class ExperimentalTestHelper {
  private static final boolean ENABLED =
      Boolean.getBoolean("otel.instrumentation.<module>.experimental-span-attributes");

  static final AttributeKey<String> SOME_KEY = stringKey("module.some_key");

  @Nullable
  static String experimental(String value) {
    return ENABLED ? value : null;
  }

  @Nullable
  static Boolean experimental(Boolean value) {
    return ENABLED ? value : null;
  }

  private ExperimentalTestHelper() {}
}
```

```java
// In test classes:
import static io.opentelemetry.javaagent.instrumentation.<module>.ExperimentalTestHelper.experimental;

equalTo(ExperimentalTestHelper.SOME_KEY, experimental("value"))
```

The `experimental()` helper method can also produce an `AttributeAssertion` using `satisfies()`
for cases where the value needs a non-equality check:

```java
static AttributeAssertion experimentalSatisfies(
    AttributeKey<String> key, StringAssertConsumer assertion) {
  if (ENABLED) {
    return satisfies(key, assertion);
  } else {
    return equalTo(key, null);
  }
}
```

### Choosing between the patterns

| Situation | Pattern |
|---|---|
| One or two experimental attributes in a single test class | Inline ternary (Pattern 1) |
| Many experimental attributes in one class | `experimental()` private method (Pattern 2) |
| Multiple test classes in the same module share the same flag | Shared `ExperimentalTestHelper` class (Pattern 2) |

---

## When to Use `assumeTrue` Instead

If an entire test only makes sense when the experimental flag is on (not just a single attribute
that switches between present/absent), guard the whole test with `assumeTrue`:

```java
@Test
void testExperimentalOnlyBehavior() {
  assumeTrue(EXPERIMENTAL_ATTRIBUTES, "Skipping: experimental attributes not enabled");
  // assertions that only apply in experimental mode
}
```

Use `assumeTrue` sparingly — prefer the ternary or `experimental()` helper pattern so the same
test body runs in both modes and verifies the flag-off (default) behaviour too.

---

## What to Flag in Review

- **`testExperimental` task missing `testClassesDirs` or `classpath`** — will silently find no
  tests.
- **`testExperimental` not wired into `check`** — never runs in CI.
- **`metadataConfig` not set on `testExperimental`** — the metadata pipeline will misattribute
  experimental-mode spans as `"default"`.
- **Experimental attribute asserted unconditionally** (no ternary, no `experimental()` wrapper,
  no `assumeTrue`) — the test will fail when run without the flag (the default `test` task).
- **Flag read inside a test method** instead of as a `private static final boolean` at class
  level — should be read once, not on every test invocation.
- **Multiple test classes sharing the same flag use inline ternaries instead of a shared helper**
  — extract an `ExperimentalTestHelper` class to avoid duplication.
