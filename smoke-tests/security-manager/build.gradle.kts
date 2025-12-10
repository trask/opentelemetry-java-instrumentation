import java.time.Duration

plugins {
  id("otel.java-conventions")
  id("com.gradleup.shadow")
}

description = "smoke-tests-security-manager"

otelJava {
  minJavaVersionSupported.set(JavaVersion.VERSION_11)
  maxJavaVersionForTests.set(JavaVersion.VERSION_11)
}

// App dependencies (for shadow JAR)
dependencies {
  implementation(platform("io.opentelemetry:opentelemetry-bom:1.0.0"))
  implementation("io.opentelemetry:opentelemetry-api")

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
      attributes["Main-Class"] = "io.opentelemetry.smoketest.securitymanager.Main"
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
    val securityManagerJarPath = shadowJarTask.flatMap { it.archiveFile }
    val securityPolicyPath = projectDir.resolve("src/main/resources/security.policy").absolutePath

    inputs.files(agentPath)
      .withPropertyName("javaagent")
      .withNormalizer(ClasspathNormalizer::class)

    inputs.files(securityManagerJarPath)
      .withPropertyName("securityManagerJar")
      .withNormalizer(ClasspathNormalizer::class)

    dependsOn(shadowJarTask)
    dependsOn(":smoke-tests:images:fake-backend:jibDockerBuild")

    jvmArgumentProviders.add(
      CommandLineArgumentProvider {
        listOf(
          "-Dio.opentelemetry.smoketest.agent.shadowJar.path=${agentPath.get()}",
          "-Dio.opentelemetry.smoketest.securitymanager.shadowJar.path=${securityManagerJarPath.get()}",
          "-Dio.opentelemetry.smoketest.securitymanager.securityPolicy.path=$securityPolicyPath"
        )
      }
    )
  }
}
