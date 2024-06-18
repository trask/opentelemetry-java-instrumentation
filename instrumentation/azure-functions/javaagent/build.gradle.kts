plugins {
  id("otel.javaagent-instrumentation")
}

dependencies {
  compileOnly(project(":instrumentation:azure-functions:compile-stub"))

  testImplementation(project(":instrumentation:azure-functions:compile-stub"))
}
