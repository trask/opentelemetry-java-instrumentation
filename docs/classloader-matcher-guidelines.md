# ClassLoader Matcher Class Selection Guide

- **Identity the instrumentation version range**: Identify the exact library version range (inclusive) that the module claims to support. Consult the module's `gradle.build.kts`, and check maven central to determine the exact release versions.
- **Compare adjacent versions**: Download the JARs from maven central for the range boundary versions. Look for classes that were introduced in the lower boundary version but missing in the version immediately prior. And look for classes that were in the upper boundary version but removed in the version immediately after.
- **Prefer stable, version-specific types**: Choose classes unlikely to move across packages or modules.
- **Verify uniqueness**: Confirm the selected class is absent from versions you want to exclude by grepping the extracted JARs or using `jar tf` listings.
- **Validate with runtime tests**: After updating the matcher, run the module's tests against both the intended version and boundary version (using `-PtestLatestDeps=true`) to confirm the matcher activates/deactivates correctly.
