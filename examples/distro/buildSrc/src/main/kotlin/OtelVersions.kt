data class OtelVersions(
  val opentelemetrySdk: String,
  val opentelemetryJavaagent: String,
  val opentelemetryJavaagentAlpha: String,
  val autoservice: String,
)

val otelVersions = OtelVersions(
  // this line is managed by .github/scripts/update-sdk-version.sh
  opentelemetrySdk = "1.61.0",

  // these lines are managed by .github/scripts/update-version.sh
  opentelemetryJavaagent = "2.28.0-SNAPSHOT",
  opentelemetryJavaagentAlpha = "2.28.0-alpha-SNAPSHOT",

  autoservice = "1.1.1",
)
