# Review Checklist

Apply the following rules when examining code. Each rule names the category label
to use in the annotation comment.

## Style guide

First, read `docs/contributing/style-guide.md` in the workspace and apply all rules found
there. The style guide is the authoritative source for: no fully qualified class names, required
static imports, visibility modifiers, `final` keyword rules, `@Nullable`, `Optional`, AssertJ,
`assumeTrue`, `@SuppressWarnings`, and hot-path performance. Do not repeat those rules below —
just cite `[Style]`, `[Visibility]`, `[Testing]`, `[API]`, or `[Performance]` and refer back to
the style guide section.

**Exception — FQN required on name collision**: When two classes with the same simple name are
both in scope (e.g., a public API type and an internal type share a name), a fully qualified
class name (FQN) for one of them is the only valid solution. **Do NOT flag such FQN usage** and
**do NOT suggest "import aliases"** — Java has no import alias feature. The FQN is correct and
necessary; annotating it as a style violation would be wrong.

---

## [Style] Static Map Initialization — Use Static Initializer, Not a Helper Method

[TODO - remove this section once it's incorporated into the style guide]

**Static field initialization**: When a `static final` field requires non-trivial initialization,
use either a `static {}` initializer block or a private static builder/factory method called
directly from the field declaration. Place the `static {}` block or builder method **immediately
after the field it initializes** — this is the established convention throughout the codebase and
keeps the initialization logic co-located with the field.

For collections/maps that need multiple entries:
- Prefer a private static builder method (`= buildFoo()`) called directly from the field
  declaration. This is the dominant pattern in the codebase and keeps the field and its
  initialization logic co-located (place the builder method immediately after the field).
- A `static {}` initializer block is also acceptable.
- For immutable collections on Java 9+ modules, `List.of()`, `Set.of()`,
  `Map.of()` / `Map.ofEntries()` are preferred as they are concise and need no helper.
- Avoid double-brace initialization (`new HashMap<>() {{ put(...); }}`).

```java
// static {} block — field and initialization co-located
private static final Map<String, String> FOO_MAP;

static {
  Map<String, String> map = new HashMap<>();
  map.put("a", "alpha");
  map.put("b", "beta");
  FOO_MAP = Collections.unmodifiableMap(map);
}

// private builder method — equally acceptable
private static final Map<String, String> FOO_MAP = buildFooMap();

private static Map<String, String> buildFooMap() {
  Map<String, String> map = new HashMap<>();
  map.put("a", "alpha");
  map.put("b", "beta");
  return map;
}

// immutable collection on Java 9+ modules
private static final Map<String, String> FOO_MAP = Map.of("a", "alpha", "b", "beta");
```

---

## [Naming] Factory Methods Must Use `create*()` Not `new*()`

Methods that construct and return objects must follow the `create*()` naming convention.
`new*()` names were deprecated in 2.25.0 and will be removed in 3.0.

- ❌ `newDecorator()` → ✅ `createDecorator()`
- ❌ `newHttpClient()` → ✅ `createHttpClient()`
- ❌ `newHandler()` → ✅ `createHandler()`

---

## [Naming] Getter Naming Consistency

All public API getter methods should start with `get`, in order to follow convention
established in OpenTelemetry Java SDK.

- ❌ `channel()` → ✅ `getChannel()`
- ❌ `remoteAddress()` → ✅ `getRemoteAddress()`
- ❌ `getDelegate()` when wrapping a request → ✅ `getRequest()`

---

## [Naming] Module and Package Naming Conventions

Read [naming-modules.md](naming-modules.md) when the PR adds a new instrumentation module,
renames a module, or adds/changes Java packages.

---

## [Javaagent] Use `Java8BytecodeBridge.currentContext()` Inside `@Advice` Methods

Inside `@Advice`-annotated methods, use `Java8BytecodeBridge.currentContext()`, NOT
`Context.current()`.

`Context.current()` is a static interface method — when inlined by ByteBuddy into pre-Java-8
bytecode it causes a `VerifyError`. `Java8BytecodeBridge.currentContext()` is a regular static
method and is safe for all class versions.

---

## [Javaagent] Do Not Throw Exceptions in Javaagent Instrumentation

Never use `throw` in javaagent instrumentation code (advice classes, `InstrumentationModule`
implementations). If a required method is missing due to a library version change, disable
the instrumentation via muzzle instead of throwing.

Library instrumentations may throw when appropriate, but javaagent instrumentations must not
break the instrumented application.

---

## [Javaagent] Semconv Constants in Library vs Javaagent

- **Library instrumentation** (`library/src/main/`): Copy semconv constants locally as
  `private static final` fields. Do NOT import from the `opentelemetry-semconv` artifact to
  avoid exposing version conflicts to end users.
  ```java
  // copied from MessagingIncubatingAttributes
  private static final AttributeKey<String> MESSAGING_SYSTEM =
      AttributeKey.stringKey("messaging.system");
  ```
- **Javaagent instrumentation** (`javaagent/src/main/`): Use semconv artifact constants
  directly — the javaagent bundles its own dependencies.
- **Tests**: Use semconv artifact constants directly.

---

## [Semconv] Dual Semconv Testing

Read [semconv-dual-testing.md](semconv-dual-testing.md) when the PR touches semconv attribute
assertions, `SemconvStability` calls, `maybeStable()` usage, or `testStableSemconv` /
`testBothSemconv` Gradle tasks.

---

## [Testing] Gradle Test Task Configuration

Read [testing-gradle-tasks.md](testing-gradle-tasks.md) when the PR adds or modifies custom
`Test` tasks in `build.gradle.kts` — covers `testClassesDirs`, `classpath`, `check` wiring,
`collectMetadata`, and `metadataConfig` requirements.

---

## [Testing] Experimental Feature Flag Tests

Read [testing-experimental-flags.md](testing-experimental-flags.md) when the PR adds or
modifies experimental attribute assertions, `testExperimental` Gradle tasks, or
`experimental-span-attributes` flag handling in test code.

---

## [API] Breaking Changes and Deprecation Policy

Read [api-deprecation-policy.md](api-deprecation-policy.md) when the PR removes or renames
public API methods, adds `@Deprecated`, or makes breaking changes to stable or alpha modules.

---

## [Config] Breaking Changes to Configuration Properties

Read [config-property-stability.md](config-property-stability.md) when the PR adds, renames,
or removes configuration properties (`otel.instrumentation.*`) — covers stability tiers,
naming conventions, deprecation cycles, and CHANGELOG requirements.

---

## [NewModule] New Instrumentation Module Checklist

If the code adds a new instrumentation module, verify ALL of the following are present:

1. **`metadata.yaml`** — must include `display_name`, `description`, `library_link`, and
   `configurations` (for each config option with `name`, `description`, `type`, `default`).
2. **`build.gradle.kts`** — `withType<Test>` block must set
   `systemProperty("collectMetadata", ...)`. Experimental test task must set
   `systemProperty("metadataConfig", "otel.instrumentation.X.option=value")`.
   See [testing-gradle-tasks.md](testing-gradle-tasks.md).
3. **Experimental attributes test task** — if there are experimental span attributes, a separate
   `testExperimental` task with `testClassesDirs`, `classpath`, the JVM arg, and `metadataConfig`.
   See [testing-experimental-flags.md](testing-experimental-flags.md).
4. **`instrumentation-docs/instrumentations.sh`** — entry for each test task variant.
5. **`settings.gradle.kts`** — `include(":instrumentation:...")` lines for all sub-projects.

---

## [Module] Module Completeness (whole-file checks)

These checks apply when reviewing a complete instrumentation module:

1. **`metadata.yaml` exists** and includes `display_name`, `description`, `library_link`, and
   `configurations`.
2. **`build.gradle.kts`** — `withType<Test>` block sets
   `systemProperty("collectMetadata", ...)`. Experimental test task sets
   `systemProperty("metadataConfig", ...)`.
3. **Experimental attributes test task** — if there are experimental span attributes, a separate
   `testExperimental` task with `testClassesDirs`, `classpath`, the JVM arg, and
   `metadataConfig`.
4. **Test classes and methods are not public** — JUnit 5 does not require public visibility.
5. **Tests use AssertJ** for assertions and JUnit 5 as the testing framework.

---

## [Kotlin] Kotlin-Specific Patterns

- Kotlin `@Advice` methods must NOT use coroutine suspension — advice code runs inline and
  cannot suspend.
- Do not use `data class` for instrumentation request/response wrapper types.
- In Kotlin library instrumentation, prefer `object` over `companion object` for static
  utility holders.
