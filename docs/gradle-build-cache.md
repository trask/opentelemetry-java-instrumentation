# Gradle Build Cache with Oracle OCI Object Storage

This repository is configured to use a remote build cache backed by Oracle OCI Object Storage (S3-compatible) to speed up builds significantly.

## How it works

The build cache is implemented using the [gradle-s3-build-cache](https://github.com/burrunan/gradle-s3-build-cache) plugin, which provides S3-compatible storage for Gradle's build cache and configuration cache.

### Configuration

The build cache is configured in `settings.gradle.kts`:
- **Remote cache**: Oracle OCI Object Storage (S3-compatible endpoint)
- **Region**: us-phoenix-1
- **Bucket**: opentelemetry-java-instrumentation-build-cache
- **Access**: Read access for everyone, write access only for main branch builds

### Access Control

#### Read Access (Pull from cache)
- Available to everyone: CI builds, pull requests, and local development
- No authentication required
- Always enabled (`isPull = true`)

#### Write Access (Push to cache)
- Only available on main branch builds (`GITHUB_REF == "refs/heads/main"`)
- Requires authentication via GitHub secrets:
  - `S3_BUILD_CACHE_ACCESS_KEY_ID`
  - `S3_BUILD_CACHE_SECRET_ACCESS_KEY`
- Disabled for pull requests and local development

### Local Development

For local development, the cache will:
- Use the local build cache when not in CI
- Read from the remote cache (no authentication needed)
- Not write to the remote cache (no credentials available)

To use the build cache locally:
```bash
./gradlew build --build-cache
```

To also enable configuration cache:
```bash
./gradlew build --build-cache --configuration-cache
```

### GitHub Actions

The cache is automatically configured in GitHub Actions workflows:
- All workflows have access to read from the cache
- Only main branch builds have write access via secrets
- Both build cache and configuration cache are enabled

### Benefits

1. **Faster builds**: Reuse compilation results and test outputs
2. **Reduced CI costs**: Less compute time needed
3. **Better developer experience**: Faster local builds
4. **Consistency**: Same artifacts across different environments

### Monitoring

Build cache effectiveness can be monitored through:
- Gradle build scans (automatically published for CI builds)
- Build times in GitHub Actions
- Cache hit rates in build logs

### Troubleshooting

If you encounter issues with the build cache:

1. **Disable build cache temporarily**:
   ```bash
   ./gradlew build --no-build-cache
   ```

2. **Clear local cache**:
   ```bash
   rm -rf ~/.gradle/caches/
   ```

3. **Check cache configuration**:
   ```bash
   ./gradlew help --build-cache
   ```

The cache is designed to be transparent - builds should work the same way with or without the cache, just faster when enabled.