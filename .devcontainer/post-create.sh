#!/bin/bash

# This script runs after the dev container is created
# It performs additional dependency pre-loading to speed up builds

echo "Setting up OpenTelemetry Java Instrumentation development environment..."

# Ensure we're in the right directory
cd /workspace

# Install pnpm for vaadin tests (Node.js installed via devcontainer features)
echo "Installing pnpm..."
npm install -g pnpm

# Download Gradle wrapper and start daemon
echo "Starting Gradle daemon..."
./gradlew --version

# Try to run the custom resolveDependencies task with timeout
echo "Pre-downloading some dependencies..."
timeout 180 ./gradlew resolveDependencies --no-daemon --parallel || echo "Dependency resolution completed or timed out"

# As a fallback, try to just compile the basic Gradle plugins
echo "Pre-compiling build scripts..."
timeout 120 ./gradlew help --no-daemon || echo "Help task completed or timed out"

echo "Development environment setup complete!"
echo "The Gradle daemon is now warmed up and build times should be faster."
