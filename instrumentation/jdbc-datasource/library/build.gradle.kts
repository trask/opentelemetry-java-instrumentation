plugins {
  id("otel.library-instrumentation")
}

dependencies {
  // depends on jdbc bootstrap for DbInfo
  implementation(project(":instrumentation:jdbc:bootstrap"))
  // depends on jdbc library for JdbcUtils and ThrowingSupplier
  implementation(project(":instrumentation:jdbc:library"))

  testImplementation(project(":instrumentation:jdbc:testing"))
}

tasks {
  withType<Test>().configureEach {
    jvmArgs("-Dotel.instrumentation.jdbc.experimental.transaction.enabled=true")
  }
}
