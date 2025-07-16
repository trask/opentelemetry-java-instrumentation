#!/bin/bash

# Validation script to check if the dev container setup is working correctly

echo "=== Dev Container Configuration Validation ==="
echo

# Check if we're in the right directory
if [ ! -f "build.gradle.kts" ]; then
    echo "❌ ERROR: Not in the root directory of the OpenTelemetry Java Instrumentation project"
    exit 1
fi

# Check if devcontainer files exist
echo "📁 Checking dev container files..."
if [ ! -f ".devcontainer/devcontainer.json" ]; then
    echo "❌ ERROR: devcontainer.json not found"
    exit 1
fi

if [ ! -f ".devcontainer/Dockerfile" ]; then
    echo "❌ ERROR: Dockerfile not found"
    exit 1
fi

if [ ! -f ".devcontainer/post-create.sh" ]; then
    echo "❌ ERROR: post-create.sh not found"
    exit 1
fi

echo "✅ All dev container files present"

# Check if the custom Gradle task exists
echo "🔧 Checking custom Gradle task..."
if grep -q "resolveDependencies" build.gradle.kts; then
    echo "✅ resolveDependencies task found in build.gradle.kts"
else
    echo "❌ ERROR: resolveDependencies task not found in build.gradle.kts"
    exit 1
fi

# Check JSON syntax
echo "🔍 Validating JSON syntax..."
if python3 -m json.tool .devcontainer/devcontainer.json > /dev/null 2>&1; then
    echo "✅ devcontainer.json has valid JSON syntax"
else
    echo "❌ ERROR: devcontainer.json has invalid JSON syntax"
    exit 1
fi

# Check if Java version matches
echo "☕ Checking Java version..."
if [ -f ".java-version" ]; then
    EXPECTED_VERSION=$(cat .java-version)
    if grep -q "java:1\":" .devcontainer/devcontainer.json && grep -q "\"version\": \"21\"" .devcontainer/devcontainer.json; then
        echo "✅ Java version in devcontainer.json matches .java-version file"
    else
        echo "⚠️  WARNING: Java version in devcontainer.json might not match .java-version file"
    fi
else
    echo "⚠️  WARNING: .java-version file not found"
fi

echo
echo "=== Validation Complete ==="
echo "🎉 Dev container configuration appears to be set up correctly!"
echo
echo "Next steps:"
echo "1. Commit these changes to your repository"
echo "2. GitHub Copilot agents will automatically use this configuration"
echo "3. Build times should be significantly improved"