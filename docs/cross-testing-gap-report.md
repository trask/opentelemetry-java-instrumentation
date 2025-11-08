# Cross-Testing Coverage Report

This document captures the audit of instrumentation libraries that ship multiple versioned `javaagent` modules and whether they cross-exercise one another via `testInstrumentation` dependencies.

## Methodology

- Parsed every `instrumentation/*/*/javaagent/build.gradle.kts` file looking for `testInstrumentation(project(":instrumentation:<lib>:…"))` entries.
- Counted libraries that expose more than one `javaagent` module and flagged those that never reference a sibling module in tests.
- Classified results into two groups: libraries already cross-testing and libraries lacking intra-family coverage.

## Findings

- Total libraries with multiple `javaagent` modules: **49**.
- Libraries that already cross-test sibling modules: **32** (examples include `couchbase`, `jedis`, `mongo`, `reactor`, `spring`).
- Libraries with no intra-library `testInstrumentation` references (gap list below): **17**.

### Libraries Missing Cross-Tests

- `armeria`: `armeria-1.3`, `armeria-grpc-1.14`
- `async-http-client`: `async-http-client-1.9`, `async-http-client-2.0`
- `aws-sdk`: `aws-sdk-1.11`, `aws-sdk-2.2`
- `clickhouse`: `clickhouse-client-common`, `clickhouse-client-v1-0.5`, `clickhouse-client-v2-0.8`
- `dropwizard`: `dropwizard-metrics-4.0`, `dropwizard-views-0.7`
- `internal`: `internal-application-logger`, `internal-class-loader`, `internal-eclipse-osgi-3.6`, `internal-lambda`, `internal-reflection`, `internal-url-class-loader`
- `jboss-logmanager`: `jboss-logmanager-appender-1.1`, `jboss-logmanager-mdc-1.1`
- `jms`: `jms-1.1`, `jms-3.0`, `jms-common`
- `jsf`: `jsf-jakarta-common`, `jsf-javax-common`, `jsf-mojarra-1.2`, `jsf-mojarra-3.0`, `jsf-myfaces-1.2`, `jsf-myfaces-3.0`
- `lettuce`: `lettuce-4.0`, `lettuce-5.0`, `lettuce-5.1`
- `liberty`: `liberty-20.0`, `liberty-dispatcher-20.0`
- `logback`: `logback-appender-1.0`, `logback-mdc-1.0`
- `okhttp`: `okhttp-2.2`, `okhttp-3.0`
- `opentelemetry-api`: `opentelemetry-api-1.0`, `1.10`, `1.15`, `1.27`, `1.31`, `1.32`, `1.37`, `1.38`, `1.4`, `1.40`, `1.42`, `1.47`, `1.50`, `1.52`
- `play`: `play-mvc`, `play-ws`
- `restlet`: `restlet-1.1`, `restlet-2.0`
- `runtime-telemetry`: `runtime-telemetry-java8`, `runtime-telemetry-java17`

## Recommended Next Steps

1. Decide which gaps warrant Couchbase-style cross-tests; add `testInstrumentation(project(":instrumentation:<lib>:<sibling>:javaagent"))` along with minimal regression coverage.
2. Where version ranges never overlap or represent helper-only modules, document the rationale or add negative tests in shared testing projects instead of cross-linking.
3. After wiring new dependencies, rerun each affected matrix (baseline + `-PtestLatestDeps=true`) to ensure matchers and muzzle checks still pass.
