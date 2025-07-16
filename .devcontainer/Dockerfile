# Use the official OpenJDK image as base
FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

# Install additional tools
RUN apt-get update && apt-get install -y \
    git \
    curl \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Set up gradle cache directory
RUN mkdir -p /home/vscode/.gradle
RUN chown -R vscode:vscode /home/vscode/.gradle

# Switch to vscode user
USER vscode

# Set JAVA_HOME for vscode user
ENV JAVA_HOME=/usr/lib/jvm/msopenjdk-current

# Create gradle cache directory
RUN mkdir -p ~/.gradle

# Final workspace setup
WORKDIR /workspace