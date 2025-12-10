import com.google.cloud.tools.jib.gradle.JibTask

plugins {
  id("otel.java-conventions")

  id("com.google.cloud.tools.jib")
  id("org.springframework.boot") version "3.5.8"
}

dependencies {
  implementation(platform("io.opentelemetry:opentelemetry-bom"))
  implementation(platform("org.springframework.boot:spring-boot-dependencies:2.6.15"))

  implementation("io.opentelemetry:opentelemetry-api")
  implementation(project(":instrumentation-annotations"))
  implementation("org.springframework.boot:spring-boot-starter-web")
}

configurations.runtimeClasspath {
  resolutionStrategy {
    // requires old logback (and therefore also old slf4j)
    force("ch.qos.logback:logback-classic:1.2.13")
    force("org.slf4j:slf4j-api:1.7.36")
  }
}

val tag = "local"
val targetJDK = project.findProperty("targetJDK") ?: "8"

java {
  // Jib detects the Java version from sourceCompatibility to determine the entrypoint format.
  // Java 8 doesn't support the @argfile syntax (added in Java 9), so Jib needs to know
  // to use an expanded classpath format instead (e.g., /app/classes:/app/libs/*).
  if (targetJDK == "8") {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
}

springBoot {
  buildInfo {
    properties {
      version = "1.2.3"
    }
  }
}

jib {
  from.image = "eclipse-temurin:$targetJDK"
  to.image = "smoke-test-spring-boot:jdk$targetJDK-$tag"
  container.ports = listOf("8080")
}

tasks {
  withType<JibTask>().configureEach {
    notCompatibleWithConfigurationCache("Jib task accesses Task.project at execution time")
  }

  val springBootJar by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
  }

  artifacts {
    add("springBootJar", bootJar)
  }
}
