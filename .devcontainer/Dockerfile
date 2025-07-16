# Use the official dev container base image
FROM mcr.microsoft.com/devcontainers/base:bookworm

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

# Create gradle cache directory
RUN mkdir -p ~/.gradle

# Final workspace setup
WORKDIR /workspace