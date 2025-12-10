import java.time.Duration

plugins {
  id("otel.java-conventions")
}

description = "smoke-tests"

otelJava {
  minJavaVersionSupported.set(JavaVersion.VERSION_11)
  maxJavaVersionForTests.set(JavaVersion.VERSION_11)
}
val dockerJavaVersion = "3.7.0"
dependencies {
  compileOnly("com.google.auto.value:auto-value-annotations")
  annotationProcessor("com.google.auto.value:auto-value")

  api("io.opentelemetry.javaagent:opentelemetry-testing-common")

  implementation(platform("io.grpc:grpc-bom:1.77.0"))
  implementation("org.slf4j:slf4j-api")
  implementation("io.opentelemetry:opentelemetry-api")
  implementation("io.opentelemetry.proto:opentelemetry-proto")
  implementation("org.testcontainers:testcontainers")
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("com.google.protobuf:protobuf-java-util:4.33.2")
  implementation("io.grpc:grpc-netty-shaded")
  implementation("io.grpc:grpc-protobuf")
  implementation("io.grpc:grpc-stub")

  implementation("com.github.docker-java:docker-java-core:$dockerJavaVersion")
  implementation("com.github.docker-java:docker-java-transport-httpclient5:$dockerJavaVersion")
}

tasks {
  test {
    testLogging.showStandardStreams = true

    // this needs to be long enough so that smoke tests that are just running slow don't time out
    timeout.set(Duration.ofMinutes(60))

    // We enable/disable smoke tests based on the java version requests
    // In addition to that we disable them on normal test task to only run when explicitly requested.
    enabled = enabled && gradle.startParameter.taskNames.any { it.startsWith(":smoke-tests:") }

    val suites = mapOf(
      "payara" to listOf("**/Payara*.*"),
      "jetty" to listOf("**/Jetty*.*"),
      "liberty" to listOf("**/Liberty*.*"),
      "tomcat" to listOf("**/Tomcat*.*"),
      "tomee" to listOf("**/Tomee*.*"),
      "websphere" to listOf("**/Websphere*.*"),
      "wildfly" to listOf("**/Wildfly*.*"),
    )

    val smokeTestSuite: String? by project
    if (smokeTestSuite != null) {
      val suite = suites[smokeTestSuite]
      if (suite != null) {
        include(suite)
      } else if (smokeTestSuite == "other") {
        suites.values.forEach {
          exclude(it)
        }
      } else if (smokeTestSuite == "none") {
        // Exclude all tests. Running this suite will compile everything needed by smoke tests
        // without executing any tests.
        exclude("**/*")
      } else {
        throw GradleException("Unknown smoke test suite: $smokeTestSuite")
      }
    }

    val shadowTask = project(":javaagent").tasks.named<Jar>("shadowJar")
    val agentJarPath = shadowTask.flatMap { it.archiveFile }
    inputs.files(agentJarPath)
      .withPropertyName("javaagent")
      .withNormalizer(ClasspathNormalizer::class)

    // Get spring boot JAR for on-demand container builds
    val springBootJarTask = project(":smoke-tests:images:spring-boot").tasks.named<Jar>("bootJar")
    val springBootJarPath = springBootJarTask.flatMap { it.archiveFile }
    inputs.files(springBootJarPath)
      .withPropertyName("springBootJar")
      .withNormalizer(ClasspathNormalizer::class)
    dependsOn(springBootJarTask)

    // Get Quarkus fast-jar distribution for on-demand container builds
    val quarkusBuildTask =
      project(":smoke-tests:images:quarkus").tasks.named("quarkusBuild")
    val quarkusDistPath =
      project(":smoke-tests:images:quarkus").layout.buildDirectory.dir("quarkus-app")
    inputs.dir(quarkusDistPath)
      .withPropertyName("quarkusDist")
    dependsOn(quarkusBuildTask)

    doFirst {
      jvmArgs("-Dio.opentelemetry.smoketest.agent.shadowJar.path=${agentJarPath.get()}")
      jvmArgs("-Dio.opentelemetry.smoketest.springboot.shadowJar.path=${springBootJarPath.get()}")
      jvmArgs("-Dio.opentelemetry.smoketest.quarkus.dist.path=${quarkusDistPath.get()}")
    }

    // Build smoke test images before running tests
    // The images to build depend on which test suite is being run
    val isWindows = System.getProperty("os.name").lowercase().contains("windows")
    val useLinuxContainers = System.getenv("USE_LINUX_CONTAINERS") == "1"
    val targetWindows = isWindows && !useLinuxContainers

    // Always build the fake backend image
    dependsOn(":smoke-tests:images:fake-backend:jibDockerBuild")
    if (targetWindows) {
      dependsOn(":smoke-tests:images:fake-backend:windowsBackendImageBuild")
    }

    // Build servlet images for app server suites
    if (smokeTestSuite in suites.keys) {
      if (targetWindows) {
        dependsOn(":smoke-tests:images:servlet:buildWindowsTestImages")
      } else {
        dependsOn(":smoke-tests:images:servlet:buildLinuxTestImages")
      }
    }

    // Note: gRPC smoke test is now in its own module (:smoke-tests:grpc)
    // Non-servlet images (spring-boot, play, etc.) use on-demand JAR copying
    // No pre-built images needed for "other" suite
  }
}
