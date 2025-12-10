# Smoke Tests

Assert that various applications will start up with the JavaAgent without any obvious ill effects.

Each subproject underneath `smoke-tests/images` contains a sample application used for testing.
The tests use Testcontainers to run these applications with the javaagent attached.

## Running Smoke Tests

Most smoke test applications are built on-demand and copied into containers at test time.
No pre-built Docker images are required for most tests.

```bash
# Run all smoke tests in the "other" suite (non-servlet apps)
./gradlew :smoke-tests:test -PsmokeTestSuite=other

# Run a specific smoke test
./gradlew :smoke-tests:test --tests "SpringBootSmokeTest"
./gradlew :smoke-tests:test --tests "GrpcSmokeTest"
```

## Smoke Test Applications

| Application | JDK Support | Build Method |
|-------------|-------------|--------------|
| spring-boot | 17, 21, 25 | bootJar copied at runtime |
| grpc | 8, 11, 17, 21, 25 | Shadow JAR copied at runtime |
| play | 17, 21, 25 | Distribution dir copied at runtime |
| quarkus | 17, 21, 25 | Fast-jar dir copied at runtime |
| security-manager | 8, 11, 17, 21 | Shadow JAR copied at runtime |
| early-jdk8 | 8 | Dockerfile built at runtime |
| servlet (app servers) | varies | Docker images built before tests |
