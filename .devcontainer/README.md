# Development Container Configuration

This directory contains the development container configuration for the OpenTelemetry Java Instrumentation project. The dev container is designed to address the issue where Copilot agents timeout due to long build times (>5 minutes) by preinstalling dependencies and optimizing the build environment.

## What this solves

- **Faster builds**: Pre-downloads Gradle dependencies and sets up optimized build configurations
- **Consistent environment**: Ensures all developers and Copilot agents use the same Java/Gradle versions
- **Reduced timeouts**: Eliminates the need to download dependencies during each build

## Files

- `devcontainer.json`: Main configuration file defining the development container
- `Dockerfile`: Custom container image with pre-installed dependencies
- `post-create.sh`: Script that runs after container creation to optimize the environment
- `README.md`: This documentation file

## How it works

1. **Base Image**: Uses Microsoft's generic dev container base with Java 21 via dev container features
2. **Post-creation Setup**: Downloads Gradle dependencies during container startup
3. **Optimization**: Sets up Gradle daemon with optimized JVM settings
4. **Node.js**: Installs Node.js 16 via dev container features for Vaadin tests
5. **Dependency Caching**: Attempts to resolve and cache dependencies after container creation

## Usage

The dev container will automatically be used by:
- GitHub Copilot agents
- VS Code with Dev Containers extension
- Any tool that supports dev containers
