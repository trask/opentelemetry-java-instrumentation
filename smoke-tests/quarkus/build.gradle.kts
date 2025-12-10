import java.time.Duration
import org.gradle.api.tasks.bundling.Jar

plugins {
  // otel.java-conventions isn't applied to this module because it adds
  // platform(project(":dependencyManagement")) which conflicts with
  // Quarkus's dependency resolution logic, causing infinite recursion in
  // GradleApplicationModelBuilder.collectDependencies during the configuration phase
  // resulting in StackOverflowError
  id("java")

  id("io.quarkus") version "3.30.2"
}

// Dependency management for test dependencies only (Quarkus BOM handles the main classpath)
// We use a separate configuration to avoid conflicts with Quarkus's dependency resolution
evaluationDependsOn(":dependencyManagement")
val dependencyManagementConf = configurations.create("dependencyManagement") {
  isCanBeConsumed = false
  isCanBeResolved = false
  isVisible = false
}
afterEvaluate {
  configurations.matching { it.name.startsWith("test") && it.isCanBeResolved && !it.isCanBeConsumed }
    .configureEach {
      extendsFrom(dependencyManagementConf)
    }
}
dependencies {
  dependencyManagementConf(platform(project(":dependencyManagement")))
}

dependencies {
  implementation(enforcedPlatform("io.quarkus:quarkus-bom:3.30.2"))
  implementation("io.quarkus:quarkus-rest")

  // Test dependencies (versions from :dependencyManagement platform)
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testImplementation("org.assertj:assertj-core")

  // Testing common with shaded dependencies (direct project reference since no otel.java-conventions substitution)
  testImplementation(project(":testing-common:with-shaded-dependencies"))

  // Additional test dependencies for smoke test runner
  // Note: platform() doesn't get versions from dependency management, so we specify explicitly
  testImplementation(platform("io.grpc:grpc-bom:1.77.0"))
  testImplementation("org.testcontainers:testcontainers")
  testImplementation("com.google.protobuf:protobuf-java-util:4.33.2")
  testImplementation("io.grpc:grpc-netty-shaded")
  testImplementation("io.grpc:grpc-protobuf")
  testImplementation("io.grpc:grpc-stub")
  val dockerJavaVersion = "3.7.0"
  testImplementation("com.github.docker-java:docker-java-core:$dockerJavaVersion")
  testImplementation("com.github.docker-java:docker-java-transport-httpclient5:$dockerJavaVersion")

  // Semantic conventions
  testImplementation("io.opentelemetry.semconv:opentelemetry-semconv")
  testImplementation("io.opentelemetry.semconv:opentelemetry-semconv-incubating")
}

// Quarkus 3.7+ requires Java 17+
java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks {
  withType<JavaCompile>().configureEach {
    with(options) {
      // Quarkus 3.7+ requires Java 17+
      release.set(17)
    }
  }

  compileJava {
    dependsOn(compileQuarkusGeneratedSourcesJava)
  }

  test {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
    timeout.set(Duration.ofMinutes(60))

    // Only run when explicitly requested (same as other smoke tests)
    enabled = enabled && gradle.startParameter.taskNames.any { it.startsWith(":smoke-tests:") }

    // Quarkus fast-jar output directory
    val quarkusFastJarDir =
      layout.buildDirectory.dir("quarkus-app").get().asFile.absolutePath

    // Pass the application distribution path to the smoke test
    systemProperty("io.opentelemetry.smoketest.quarkus.dist.path", quarkusFastJarDir)

    // Build the Quarkus app before running the smoke test
    dependsOn("quarkusBuild")

    // Build fake-backend image before running smoke tests
    dependsOn(":smoke-tests:images:fake-backend:jibDockerBuild")

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

// Get smoke-tests project's source sets for the test runner infrastructure
val smokeTestsProject = evaluationDependsOn(":smoke-tests")

dependencies {
  testImplementation(smokeTestsProject.sourceSets["main"].output)
  testImplementation(smokeTestsProject.sourceSets["test"].output)
}
