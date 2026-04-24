# [API] Breaking Changes and Deprecation Policy

## Quick Reference

- Use when: reviewing public API removals/renames, `@Deprecated` usage, stable-vs-alpha compatibility, or any module rename that touches user-facing config keys or emitted telemetry identity
- Review focus: deprecate-then-remove timing, delegation direction, required Javadoc/CHANGELOG coverage, v3-preview gating for config keys and scope names

## What Counts as "Public API"

"API" here means **anything a user's code or configuration depends on by name**, including:

- Java symbols in published artifacts (classes, methods, fields in `:library`, `:testing`,
  `instrumentation-api*`).
- User-facing configuration keys — `otel.instrumentation.<name>.enabled`, any
  `otel.instrumentation.*` property, and the equivalent declarative YAML keys.
- Outgoing telemetry identity — anything users can match on in their backend, including
  `otel.scope.name`, span names, metric names, attribute keys, and attribute values. Users
  build dashboards, filters, and alerts on these values, so silently renaming them is a
  breaking change.

A rename of any of these surfaces is a breaking change even if no Java symbol moved.

## When Are Breaking Changes Allowed?

Only in **non-stable (alpha) modules** — i.e. artifacts whose version has the `-alpha` suffix.
Stable module APIs follow strict backwards compatibility:

- Items in stable modules **can be deprecated** in any minor release.
- Deprecated items in stable modules **cannot be removed until the next major version** (currently targeting 3.0).

The CHANGELOG uses distinct headings to distinguish:

- `⚠️ Breaking changes to non-stable APIs` — alpha/non-stable modules (routine)
- `⚠️ Breaking Changes` — stable module changes (rare, requires strong justification)

### Javaagent modules are not a public API

Javaagent modules (Gradle path ends with `:javaagent`, including shared `-common` javaagent
modules) are bundled into the agent jar and are **not** published for external consumption.
Do **not** apply a deprecation cycle to symbols in javaagent modules — rename or change the
API directly and update all in-repo callers in the same commit. The deprecate-then-remove
cycle described below applies only to non-stable modules whose artifacts are published
(e.g., `:library`, `:testing`, `instrumentation-api*`).

## The Deprecate-Then-Remove Cycle

### Alpha (non-stable) modules

Deprecations in alpha modules are introduced in one monthly release and removed in a subsequent
one. The gap is typically **one release** (approximately one month).

### Stable modules

Deprecations in stable modules accumulate over multiple releases and are only **removed in the
next major version** (3.0). Many items carry `// to be removed in 3.0` or `@deprecated ... Will
be removed in 3.0` comments to make this explicit.

## Correct `@Deprecated` Usage

```java
/**
 * @deprecated Use {@link #newMethod()} instead. Will be removed in a future release.
 */
@Deprecated // will be removed in X.Y
public ReturnType oldMethod() {
  return newMethod();  // delegate to the replacement
}
```

Rules:

- Use plain `@Deprecated` — do **not** use `forRemoval=true` or `since="..."` (must stay Java 8 compatible).
- Always include a `@deprecated` Javadoc tag that names the replacement and states the removal timeline.
- An inline comment (`// will be removed in X.Y` or `// to be removed in 3.0`) is strongly encouraged.
- The **deprecated method must delegate to its replacement**, not the other way around. This ensures
  anyone overriding the deprecated method still gets called.
- Add the `deprecation` label to the PR — this drives the automated `🚫 Deprecations` CHANGELOG entry.

### Deprecating default interface methods

The same pattern applies; just add `default` to keep the method callable during the transition:

```java
/**
 * @deprecated Use {@link #configure(IgnoredTypesBuilder)} instead.
 */
@Deprecated
default void configure(IgnoredTypesBuilder builder, ConfigProperties config) {
  configure(builder);
}
```

## Module renames: config keys and emitted scope names

A module rename touches **two user-facing API surfaces** that must each be preserved by default
and only change under `otel.instrumentation.common.v3-preview`. Both cases use
`AgentCommonConfig.get().isV3Preview()` to gate the switch, but the mechanism differs because
the surfaces are different: config keys can coexist as aliases, emitted scope names cannot.

### 1. `InstrumentationModule` names (controls `otel.instrumentation.<name>.enabled`)

The names passed to the `InstrumentationModule` constructor drive the
`otel.instrumentation.<name>.enabled` config keys. A rename silently breaks users who have
the old key in their config.

Keep the pre-rename name as an **additional alias at the end of the list**, and drop it under
v3-preview:

```java
public CxfInstrumentationModule() {
  super("cxf", additionalNames());
}

private static String[] additionalNames() {
  if (AgentCommonConfig.get().isV3Preview()) {
    return new String[] {"jaxws-2.0-cxf-3.0", "jaxws"};
  }
  return new String[] {"jaxws-2.0-cxf-3.0", "jaxws", "jaxws-cxf-3.0"};
}
```

Order matters: `AgentDistributionConfig#isInstrumentationEnabled` checks names in the order
given and returns on the first explicit setting. Put the alias **last** so the new name wins
when both are set.

### 2. Emitted instrumentation scope name (`INSTRUMENTATION_NAME` in `*Singletons`)

The `INSTRUMENTATION_NAME` string passed to `Instrumenter.builder(...)` becomes the
`otel.scope.name` attribute on every emitted span / metric / log. A rename silently breaks
dashboards and filters that match on the old scope.

Keep emitting the **pre-rename** scope name by default, and switch to the new one only under
v3-preview:

```java
public class CxfSingletons {
  private static final String INSTRUMENTATION_NAME =
      AgentCommonConfig.get().isV3Preview()
          ? "io.opentelemetry.jaxws-2.0-cxf-3.0"
          : "io.opentelemetry.jaxws-cxf-3.0";
  ...
}
```

Note the asymmetry with the config-key case: there the list contains **both** names at once
(aliases); here only **one** scope name is emitted at a time, and the default is the **old**
one.

### CHANGELOG

The rename is **not** a breaking change or a deprecation in the current release: by default
the old config keys and scope names continue to work unchanged, and the new names are only
visible under `otel.instrumentation.common.v3-preview` — a preview flag that users are not
generally encouraged to enable. Do not add a `⚠️ Breaking changes to non-stable APIs` or
`🚫 Deprecations` entry for this kind of rename.
The breaking change will be recorded when v3-preview becomes the default in 3.0.

## What to Flag in Review

- **Breaking change without a prior deprecation**: a method/class was removed or its signature
  changed in a stable module, but there was no `@Deprecated` annotation in the preceding release.
  Flag and ask for the deprecation to be introduced first.

- **Removal of a deprecated item from a stable module before 3.0**: deprecated items in stable
  modules must not be removed in a minor release — they stay until the next major version.

- **`@Deprecated` without Javadoc**: annotation present but no `@deprecated` Javadoc, or the
  Javadoc doesn't name the replacement — ask for both.

- **Wrong delegation direction**: the new method delegates to the old/deprecated one instead of
  the reversed. This breaks overriders of the old method.

- **Deprecated method with new logic**: instead of delegating, it reimplements. The logic should
  live in the new method.

- **Removal PR for things never deprecated**: a removal PR must only remove things that were
  already annotated `@Deprecated` in an earlier release.

- **Missing CHANGELOG entry**: a breaking change PR that does not add an
  `⚠️ Breaking changes to non-stable APIs` bullet in the `Unreleased` section of `CHANGELOG.md`.

- **Module rename without backcompat**: `InstrumentationModule` constructor doesn't list the
  pre-rename name as an alias, and/or `*Singletons#INSTRUMENTATION_NAME` was changed
  unconditionally instead of being gated on `AgentCommonConfig.get().isV3Preview()`.
