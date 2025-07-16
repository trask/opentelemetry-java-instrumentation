# Use the official dev container base image
FROM mcr.microsoft.com/devcontainers/base:bookworm

# Set up gradle cache directory
RUN mkdir -p /home/vscode/.gradle
RUN chown -R vscode:vscode /home/vscode/.gradle

# Switch to vscode user
USER vscode

# Create gradle cache directory
RUN mkdir -p ~/.gradle

# Final workspace setup
WORKDIR /workspace