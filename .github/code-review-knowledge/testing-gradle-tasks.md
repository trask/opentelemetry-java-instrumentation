# [Testing] Gradle Test Task Configuration

## The Mandatory Properties

Every custom `Test` task registered with `val foo by registering(Test::class)` **must** include
`testClassesDirs` and `classpath`. Without them the task discovers no test classes and passes
vacuously — a silent false-negative.

```kotlin
val testFoo by registering(Test::class) {
  testClassesDirs = sourceSets.test.get().output.classesDirs
  classpath = sourceSets.test.get().runtimeClasspath
  // ... other config
}
```

Every such task **must** also be wired into the `check` lifecycle:

```kotlin
check {
  dependsOn(testFoo)
}
```

---

## `collectMetadata` and `metadataConfig`

These two system properties work together for the instrumentation metadata collection pipeline
(used to generate `instrumentation-list.yaml` and related docs). They are not required for
correctness of the tests themselves but **should be included** on every custom test task that
runs under a non-default configuration.

| Property | Type | Value |
|---|---|---|
| `collectMetadata` | System property | Pass-through of the `collectMetadata` Gradle project property; defaults to `"false"` |
| `metadataConfig` | System property | A single `key=value` string describing the non-default configuration active during this test run |

**`collectMetadata`** tells the `MetaDataCollector` test infrastructure whether to record span
attribute metadata for the current test run. Always set it from the project property:

```kotlin
systemProperty("collectMetadata", findProperty("collectMetadata")?.toString() ?: "false")
```

When multiple tasks share the same value, extract it into a top-level variable:

```kotlin
val collectMetadata = findProperty("collectMetadata")?.toString() ?: "false"
```

**`metadataConfig`** labels the variant so the collector can bucket metadata by configuration.
The value is an arbitrary `key=value` string that describes what makes this task's configuration
distinct from the default:

```kotlin
// semconv variant:
systemProperty("metadataConfig", "otel.semconv-stability.opt-in=database")

// experimental flag variant:
systemProperty("metadataConfig", "otel.instrumentation.spymemcached.experimental-span-attributes=true")

// multiple properties (comma-separated):
systemProperty("metadataConfig", "otel.semconv-stability.opt-in=database,otel.foo.bar=true")
```

The `test` (default) task does **not** set `metadataConfig` — its metadata is collected under
the label `"default"` automatically.

---

## `withType<Test>().configureEach` vs individual tasks

Some modules apply `collectMetadata` globally rather than per-task:

```kotlin
tasks {
  withType<Test>().configureEach {
    systemProperty("collectMetadata", findProperty("collectMetadata")?.toString() ?: "false")
  }
  // ... individual tasks only need metadataConfig
}
```

This is fine when all tasks in the module should honour the flag. The per-task approach (shown
in the canonical examples above) is equally valid and preferred when there are only a few tasks.

---

## What to Flag in Review

- **Missing `testClassesDirs` or `classpath`** on a `registering(Test::class)` task — the task
  will silently find no tests.
- **Custom test task not wired into `check`** — it will never run in CI.
- **`metadataConfig` missing on a non-default task** — the metadata pipeline will misattribute
  that variant's spans as belonging to the `"default"` configuration.
- **`collectMetadata` missing on a non-default task** — metadata collection silently skipped for
  that variant when the metadata pipeline is run.
- **`testBothSemconv` present without `testStableSemconv`** — the `dup` task covers both
  attributes simultaneously, but the stable-only scenario also needs its own task.
