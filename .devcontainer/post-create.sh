#!/bin/bash

# This script runs after the dev container is created
# It performs additional dependency pre-loading to speed up builds

echo "Setting up OpenTelemetry Java Instrumentation development environment..."

# Ensure we're in the right directory
cd /workspace

# Download Gradle wrapper and basic dependencies
echo "Downloading Gradle and basic dependencies..."
./gradlew --version

# Pre-download dependencies by running a lightweight task
echo "Pre-downloading project dependencies..."
./gradlew resolveDependencies --no-daemon --parallel || true

# Try to compile basic components without running tests
echo "Pre-compiling basic components..."
./gradlew compileJava -x test -x check -x spotlessCheck -PskipTests=true --parallel || true

echo "Development environment setup complete!"
echo "Build times should now be significantly faster."