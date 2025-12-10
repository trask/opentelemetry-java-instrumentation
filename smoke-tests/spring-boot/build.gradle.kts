import java.time.Duration

plugins {
  id("otel.java-conventions")

  id("org.springframework.boot") version "3.5.8"
}

description = "smoke-tests-spring-boot"

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

  // Test dependencies - need test classes from smoke-tests for AbstractSmokeTest
  testImplementation(project(":smoke-tests"))
}

springBoot {
  buildInfo {
    properties {
      version = "1.2.3"
    }
  }
}

val smokeTestsProject = evaluationDependsOn(":smoke-tests")

dependencies {
  testImplementation(smokeTestsProject.sourceSets["test"].output)
}

tasks {
  test {
    testLogging.showStandardStreams = true
    timeout.set(Duration.ofMinutes(60))

    // Only run when explicitly requested (same as other smoke tests)
    enabled = enabled && gradle.startParameter.taskNames.any { it.startsWith(":smoke-tests:") }

    // Build the Spring Boot fat JAR before running the smoke test
    val bootJarTask = named("bootJar")
    dependsOn(bootJarTask)

    // Build fake-backend image before running smoke tests
    dependsOn(":smoke-tests:images:fake-backend:jibDockerBuild")

    // Pass the Spring Boot JAR path to the smoke test
    val bootJarPath = bootJarTask.flatMap { it.outputs.files.singleFile.let { file -> provider { file.absolutePath } } }
    inputs.files(bootJarTask)
      .withPropertyName("springBootJar")
      .withNormalizer(ClasspathNormalizer::class)

    doFirst {
      systemProperty("io.opentelemetry.smoketest.springboot.shadowJar.path", bootJarPath.get())
    }

    // Javaagent jar configuration (configuration-cache safe)
    val agentJarTask = project(":javaagent").tasks.named<Jar>("shadowJar")
    val agentPath = agentJarTask.flatMap { it.archiveFile }
    inputs.files(agentPath)
      .withPropertyName("javaagent")
      .withNormalizer(ClasspathNormalizer::class)

    // Pass the javaagent jar path to the smoke test
    jvmArgumentProviders.add(
      CommandLineArgumentProvider {
        listOf(
          "-Dio.opentelemetry.smoketest.agent.shadowJar.path=${agentPath.get().asFile.absolutePath}"
        )
      }
    )
  }
}
