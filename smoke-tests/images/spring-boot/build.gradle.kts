plugins {
  id("otel.java-conventions")

  id("org.springframework.boot") version "3.5.8"
}

// Spring Boot 3.x requires Java 17+
otelJava {
  minJavaVersionSupported.set(JavaVersion.VERSION_17)
}

dependencies {
  implementation(platform("io.opentelemetry:opentelemetry-bom"))
  implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))

  implementation("io.opentelemetry:opentelemetry-api")
  implementation(project(":instrumentation-annotations"))
  implementation("org.springframework.boot:spring-boot-starter-web")
}

springBoot {
  buildInfo {
    properties {
      version = "1.2.3"
    }
  }
}

// The bootJar is copied into the test container at runtime by Testcontainers.
// No pre-built Docker image needed - tests use base JDK images with the JAR copied in.
// Note: Spring Boot 3.x requires JDK 17+, so this smoke test only runs on JDK 17+.
