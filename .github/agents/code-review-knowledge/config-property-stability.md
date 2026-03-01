# [Config] Configuration Property Stability and Breaking Changes

## The Two Tiers of Stability

Configuration properties in this project have two distinct stability tiers, defined in
[VERSIONING.md](../../VERSIONING.md):

| Tier | Identifying pattern | Breaking changes allowed? |
|------|--------------------|--------------------------:|
| **Stable** | No `experimental` in the name and not under `otel.javaagent.testing.*` | ❌ No — can be deprecated in a minor release, but **removal requires a major version bump** (3.0) |
| **Experimental** | Contains the word `experimental` anywhere in the name | ✅ Yes — must still be deprecated before removal, but can be removed in the very next release |

Examples:
- `otel.instrumentation.http.client.capture-request-headers` — **stable**, can be deprecated in a minor release but cannot be removed until 3.0
- `otel.instrumentation.common.experimental.controller-telemetry.enabled` — **experimental**, can be changed freely
- `otel.instrumentation.http.client.emit-experimental-telemetry` — **experimental** (word appears in name)
- `otel.javaagent.testing.*` — always allowed to break, regardless of name

---

## Stable Property: Deprecate-Then-Remove Cycle

Stable (non-experimental) properties can be deprecated in any minor release, but **cannot be
removed until the next major version** (currently 3.0). This is stricter than experimental
properties (see below).

Deprecation must be declared before 3.0 removes the property.

## Experimental Property: Deprecate-Then-Remove Cycle

Experimental properties still require a deprecation step before removal — they cannot simply
disappear without warning. However, the window is much shorter: the property must be noted as
deprecated in one release and **can be removed in the very next release**.

The same communication mechanisms apply (see below).

Because config properties are string literals with no `@Deprecated` annotation mechanism, the
deprecation is communicated through:
1. A `🚫 Deprecations` CHANGELOG entry naming the old and new property.
2. A comment in code near where the old property is read.
3. Optionally: reading both old and new property names and preferring the new one during the
   transition window.

Real examples of the deprecate-then-remove cycle from the CHANGELOG (all experimental properties
following the one-release window):

| Old property | New property | Deprecated | Removed |
|---|---|---|---|
| `otel.instrumentation.logback-appender.experimental.capture-logstash-attributes` | `...capture-logstash-marker-attributes` | 2.21.0 | 2.24.0 |
| `otel.instrumentation.http.capture-headers.client.request` | `otel.instrumentation.http.client.capture-request-headers` | earlier | 2.1.0 |
| `otel.instrumentation.http.client.emit-experimental-metrics` | `...emit-experimental-telemetry` | earlier | 2.1.0 |
| `otel.instrumentation.log4j-appender.experimental.capture-context-data-attributes` | `...capture-mdc-attributes` | earlier | 2.2.0 |

---

## Deprecation Communication (No Automatic Redirect)

Unlike Java API deprecations where the old method can delegate to the new one, **config properties
have no automatic forwarding mechanism**. The deprecation is communicated through:

1. A `🚫 Deprecations` CHANGELOG entry naming the old and new property.
2. A comment in code near where the old property is read.
3. **A `WARN`-level log message emitted at startup** if the deprecated property name is detected
   in the user's configuration. This is required when writing a deprecation — do not rely only on
   the CHANGELOG. Example pattern:
   ```java
   if (config.getString("otel.instrumentation.foo.old-property-name") != null) {
     logger.warn(
         "otel.instrumentation.foo.old-property-name is deprecated and will be removed in a "
             + "future release. Use otel.instrumentation.foo.new-property-name instead.");
   }
   ```
   > **Note**: the startup warning infrastructure is not yet universally implemented across the
   > codebase (there is a `// TODO when logging is configured add warning about deprecated property`
   > comment in `ExtensionClassLoader`). Until it is, the code that reads the fallback value should
   > include the `@SuppressWarnings("deprecation") // using deprecated config property` annotation
   > as a marker.

Make config property renames as visible as possible — the CHANGELOG alone is not sufficient.

---

## Migration Support Pattern (Optional)

During the deprecation window, code may read both old and new names to ease migration. The
canonical pattern is to prefer the new name but fall back to the old:

```java
// Prefer new property; fall back to old (deprecated) property during transition
Boolean value = config.getBoolean("otel.instrumentation.foo.new-property-name");
if (value == null) {
  value = config.getBoolean("otel.instrumentation.foo.old-property-name");
}
```

This pattern is not required — some renames just drop the old property immediately once the
deprecation window has closed.

---

## Naming Conventions for New Properties

Follow the existing naming conventions when adding configuration properties:

- **Prefix**: always `otel.instrumentation.<module-name>.` for per-instrumentation properties,
  or `otel.instrumentation.common.` for cross-cutting properties.
- **Separator**: hyphens between words (kebab-case), e.g. `capture-request-headers`.
- **Experimental marker**: if the feature is experimental, include `experimental` in the name:
  `otel.instrumentation.<module>.experimental.<feature-name>`.
- **Boolean toggles**: use `.enabled` suffix for boolean on/off flags, e.g.
  `otel.instrumentation.kafka.producer-propagation.enabled`.
- **Env var equivalent**: dots and hyphens convert to underscores in ALL_CAPS, e.g.
  `otel.instrumentation.foo.bar-baz` ↔ `OTEL_INSTRUMENTATION_FOO_BAR_BAZ`.

---

## What to Flag in Review

- **Stable property removed in a minor release**: stable properties cannot be removed before 3.0, regardless of whether they were previously deprecated. Flag immediately.
- **Stable property deprecated without a CHANGELOG entry**: the `🚫 Deprecations` entry is required even when the actual removal is deferred to 3.0.
- **Stable property renamed in a single PR** (old removed, new added at once): the old property must remain (deprecated) until 3.0. Use the fall-back-to-old pattern during the deprecation window.
- **Property name doesn't follow kebab-case or the `otel.instrumentation.*` prefix convention**.
- **New experimental feature using a non-experimental property name**: a feature that may change
  should carry `experimental` in its property name so users know it can break.
- **Removal PR for a stable property that was only deprecated in the same PR** (zero deprecation
  window): the CHANGELOG must clearly state this is a same-release removal, which requires
  strong justification.
