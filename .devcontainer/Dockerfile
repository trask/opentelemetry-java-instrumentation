# Use the official OpenJDK image as base
FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

# Install additional tools
RUN apt-get update && apt-get install -y \
    git \
    curl \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Set up Node.js (needed for vaadin tests)
RUN curl -fsSL https://deb.nodesource.com/setup_16.x | bash - \
    && apt-get install -y nodejs

# Install pnpm (needed for vaadin tests)
RUN npm install -g pnpm

# Set up gradle cache directory
RUN mkdir -p /home/vscode/.gradle
RUN chown -R vscode:vscode /home/vscode/.gradle

# Switch to vscode user
USER vscode

# Set JAVA_HOME for vscode user
ENV JAVA_HOME=/usr/lib/jvm/msopenjdk-current

# Pre-configure git (optional, helps with development)
RUN git config --global init.defaultBranch main

# Pre-configure gradle with optimized settings
RUN mkdir -p ~/.gradle && \
    echo "org.gradle.daemon=true" > ~/.gradle/gradle.properties && \
    echo "org.gradle.parallel=true" >> ~/.gradle/gradle.properties && \
    echo "org.gradle.caching=true" >> ~/.gradle/gradle.properties && \
    echo "org.gradle.configureondemand=true" >> ~/.gradle/gradle.properties && \
    echo "org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m" >> ~/.gradle/gradle.properties && \
    echo "org.gradle.priority=low" >> ~/.gradle/gradle.properties

# Set up pnpm store
RUN mkdir -p ~/.pnpm-store

# Final workspace setup
WORKDIR /workspace