group = "io.opentelemetry.example"
version = "1.0-SNAPSHOT"

subprojects {
  version = rootProject.version

  apply(plugin = "otel.java-conventions")
}
