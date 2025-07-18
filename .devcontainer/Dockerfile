# Use the same base image as the original devcontainer
FROM mcr.microsoft.com/vscode/devcontainers/base:bullseye

# Install Temurin OpenJDK 21 and Docker
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
        wget \
        ca-certificates \
        curl \
        gnupg \
        lsb-release \
    && wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add - \
    && echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list \
    && curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null \
    && apt-get update \
    && apt-get -y install --no-install-recommends \
        temurin-21-jdk \
        docker-ce \
        docker-ce-cli \
        containerd.io \
    && usermod -aG docker vscode `# Add vscode user to docker group for Docker access` \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Switch to vscode user for the build
USER vscode

# Set working directory for build
WORKDIR /tmp/build

# Copy all project files for the build
COPY --chown=vscode:vscode . ./

# Pre-populate Gradle dependency and build caches by compiling all classes
# Use --no-daemon to avoid leaving daemon processes running
# Set JAVA_HOME for Temurin OpenJDK 21
RUN export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 && \
    export PATH="$JAVA_HOME/bin:$PATH" && \
    ./gradlew classes --no-daemon

# Clean up the build directory but preserve gradle cache
RUN rm -rf /tmp/build

# Set default working directory for development
WORKDIR /workspaces
