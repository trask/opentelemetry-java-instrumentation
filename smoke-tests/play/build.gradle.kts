import java.time.Duration
import org.gradle.jvm.toolchain.JavaLanguageVersion
import play.gradle.Language

plugins {
  // Don't apply java-conventions since no Java in this project and it interferes with play plugin.
  id("otel.spotless-conventions")
  id("org.playframework.play") version "3.1.0-M4"
}

evaluationDependsOn(":dependencyManagement")
val dependencyManagementConf = configurations.create("dependencyManagement") {
  isCanBeConsumed = false
  isCanBeResolved = false
  isVisible = false
}
afterEvaluate {
  configurations.configureEach {
    if (isCanBeResolved && !isCanBeConsumed) {
      extendsFrom(dependencyManagementConf)
    }
  }
}
dependencies {
  dependencyManagementConf(platform(project(":dependencyManagement")))
}

play {
  lang.set(Language.SCALA)
}

dependencies {
  val playVersion = "3.1.0-M4"
  val scalaVersion = "2.13"

  implementation("org.playframework:play-guice_$scalaVersion:$playVersion")
  implementation("org.playframework:play-logback_$scalaVersion:$playVersion")
  implementation("org.playframework:play-filters-helpers_$scalaVersion:$playVersion")
  runtimeOnly("org.playframework:play-server_$scalaVersion:$playVersion")
  runtimeOnly("org.playframework:play-pekko-http-server_$scalaVersion:$playVersion")
  runtimeOnly("org.apache.pekko:pekko-http_$scalaVersion:1.3.0")

  // Test dependencies
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testImplementation("org.assertj:assertj-core")
  // Use project reference directly - external GAV coordinates need otel.java-conventions for substitution
  testImplementation(project(":testing-common:with-shaded-dependencies"))

  // Runtime dependencies needed by smoke-tests infrastructure
  val dockerJavaVersion = "3.7.0"
  testImplementation(platform("io.grpc:grpc-bom:1.77.0"))
  testImplementation("org.slf4j:slf4j-api")
  testImplementation("io.opentelemetry:opentelemetry-api")
  testImplementation("io.opentelemetry.proto:opentelemetry-proto")
  testImplementation("org.testcontainers:testcontainers")
  testImplementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("com.google.protobuf:protobuf-java-util:4.33.2")
  testImplementation("io.grpc:grpc-netty-shaded")
  testImplementation("io.grpc:grpc-protobuf")
  testImplementation("io.grpc:grpc-stub")
  testImplementation("com.github.docker-java:docker-java-core:$dockerJavaVersion")
  testImplementation("com.github.docker-java:docker-java-transport-httpclient5:$dockerJavaVersion")
}

val smokeTestsProject = evaluationDependsOn(":smoke-tests")

dependencies {
  // smoke-tests has the base testing infrastructure (SmokeTestRunner in main, AbstractSmokeTest in test)
  // We pull in the outputs directly to avoid transitive dependency resolution issues
  testImplementation(smokeTestsProject.sourceSets["main"].output)
  testImplementation(smokeTestsProject.sourceSets["test"].output)
}

val targetJDK = (project.findProperty("targetJDK") as String?) ?: "17"
val javaLanguageVersion = targetJDK.toIntOrNull() ?: 17

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(javaLanguageVersion))
}

tasks {
  test {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
    timeout.set(Duration.ofMinutes(60))

    // Only run when explicitly requested
    enabled = enabled && gradle.startParameter.taskNames.any { it.startsWith(":smoke-tests:") }

    val agentJarTask = project(":javaagent").tasks.named<Jar>("shadowJar")
    val installDistTask = named("installDist")
    val agentPath = agentJarTask.flatMap { it.archiveFile }
    val playDistPath = layout.buildDirectory.dir("install/play")

    inputs.files(agentPath)
      .withPropertyName("javaagent")
      .withNormalizer(ClasspathNormalizer::class)

    inputs.dir(playDistPath)
      .withPropertyName("playDist")

    dependsOn(installDistTask)
    dependsOn(":smoke-tests:images:fake-backend:jibDockerBuild")

    jvmArgumentProviders.add(
      CommandLineArgumentProvider {
        listOf(
          "-Dio.opentelemetry.smoketest.agent.shadowJar.path=${agentPath.get()}",
          "-Dio.opentelemetry.smoketest.play.dist.path=${playDistPath.get()}"
        )
      }
    )
  }
}
