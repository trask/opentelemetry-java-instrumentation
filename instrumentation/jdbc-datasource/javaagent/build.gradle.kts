plugins {
  id("otel.javaagent-instrumentation")
}

muzzle {
  pass {
    coreJdk()
  }
}

dependencies {
  bootstrap(project(":instrumentation:jdbc:bootstrap"))
  compileOnly(project(":instrumentation:jdbc:library"))
  implementation(project(":instrumentation:jdbc-datasource:library"))

  testImplementation(project(":instrumentation:jdbc:testing"))
  testInstrumentation(project(":instrumentation:jdbc:javaagent"))

  // jdbc unit testing
  testLibrary("com.h2database:h2:1.3.169")
  testLibrary("org.apache.tomcat:tomcat-jdbc:7.0.19")
  // tomcat needs this to run
  testLibrary("org.apache.tomcat:tomcat-juli:7.0.19")
  testLibrary("com.zaxxer:HikariCP:2.4.0")
  testLibrary("com.mchange:c3p0:0.9.5")
  testLibrary("com.alibaba:druid:1.2.20")
}

sourceSets {
  main {
    val shadedDep = project(":instrumentation:jdbc:library")
    output.dir(
      shadedDep.file("build/extracted/shadow-javaagent"),
      "builtBy" to ":instrumentation:jdbc:library:extractShadowJarJavaagent",
    )
  }
}

tasks {
  withType<Test>().configureEach {
    jvmArgs("-Dotel.instrumentation.jdbc-datasource.enabled=true")
    jvmArgs("-Dotel.instrumentation.jdbc.experimental.transaction.enabled=true")
  }
}
