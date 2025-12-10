# Smoke Tests

Assert that various applications will start up with the JavaAgent without any obvious ill effects.

Each subproject underneath `smoke-tests` produces one or more docker images containing some application
under test. Various tests in the main module then use them to run the appropriate tests.

## Building Smoke Test Images

The smoke test images are built on-demand locally before running tests. They are not published to any
container registry.

To build and run smoke tests locally:

```bash
# Run smoke tests (images are built automatically via task dependencies)
./gradlew :smoke-tests:test -PextraTag=local

# Or build images explicitly first
./gradlew :smoke-tests:images:spring-boot:jibDockerBuild -PtargetJDK=8 -PextraTag=local
./gradlew :smoke-tests:images:spring-boot:jibDockerBuild -PtargetJDK=11 -PextraTag=local
# etc.
```

The `-PextraTag=local` property sets the image tag that the tests will use when looking for images.
