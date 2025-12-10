import java.time.Duration

plugins {
  id("otel.java-conventions")
  id("com.gradleup.shadow")
}

description = "smoke-tests-grpc"

otelJava {
  minJavaVersionSupported.set(JavaVersion.VERSION_11)
  maxJavaVersionForTests.set(JavaVersion.VERSION_11)
}

// App dependencies (for shadow JAR)
dependencies {
  implementation(platform("io.grpc:grpc-bom:1.77.0"))
  implementation(platform("io.opentelemetry:opentelemetry-bom:1.0.0"))
  implementation(platform("io.opentelemetry:opentelemetry-bom-alpha:1.0.0-alpha"))
  implementation(platform("org.apache.logging.log4j:log4j-bom:2.25.2"))

  implementation("io.grpc:grpc-netty-shaded")
  implementation("io.grpc:grpc-protobuf")
  implementation("io.grpc:grpc-stub")
  implementation("io.opentelemetry.proto:opentelemetry-proto")
  implementation(project(":instrumentation-annotations"))
  implementation("org.apache.logging.log4j:log4j-core")

  runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl")

  // Test dependencies - need test classes from smoke-tests for AbstractSmokeTest
  testImplementation(project(":smoke-tests"))
}

val smokeTestsProject = evaluationDependsOn(":smoke-tests")

dependencies {
  testImplementation(smokeTestsProject.sourceSets["test"].output)
}

// Compile app to Java 8 so the same JAR can be tested on all JDK versions
tasks.withType<JavaCompile>().configureEach {
  if (name == "compileJava") {
    options.release.set(8)
  }
}

tasks {
  shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()

    manifest {
      attributes["Main-Class"] = "io.opentelemetry.smoketest.grpc.TestMain"
    }
  }

  test {
    testLogging.showStandardStreams = true
    timeout.set(Duration.ofMinutes(60))

    // Only run when explicitly requested
    enabled = enabled && gradle.startParameter.taskNames.any { it.startsWith(":smoke-tests:") }

    val agentJarTask = project(":javaagent").tasks.named<Jar>("shadowJar")
    val shadowJarTask = shadowJar
    val agentPath = agentJarTask.flatMap { it.archiveFile }
    val grpcJarPath = shadowJarTask.flatMap { it.archiveFile }

    inputs.files(agentPath)
      .withPropertyName("javaagent")
      .withNormalizer(ClasspathNormalizer::class)

    inputs.files(grpcJarPath)
      .withPropertyName("grpcJar")
      .withNormalizer(ClasspathNormalizer::class)

    dependsOn(shadowJarTask)
    dependsOn(":smoke-tests:images:fake-backend:jibDockerBuild")

    jvmArgumentProviders.add(
      CommandLineArgumentProvider {
        listOf(
          "-Dio.opentelemetry.smoketest.agent.shadowJar.path=${agentPath.get()}",
          "-Dio.opentelemetry.smoketest.grpc.shadowJar.path=${grpcJarPath.get()}"
        )
      }
    )
  }
}
