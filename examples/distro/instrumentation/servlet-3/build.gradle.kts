import org.gradle.process.CommandLineArgumentProvider

plugins {
  id("com.gradleup.shadow")
  id("io.opentelemetry.instrumentation.muzzle-generation")
  id("io.opentelemetry.instrumentation.muzzle-check")
}

val otelVersions = the<OtelVersions>()

val autoservice = listOf(
  "com.google.auto.service:auto-service:${otelVersions.autoservice}",
  "com.google.auto.service:auto-service-annotations:${otelVersions.autoservice}",
)

val testInstrumentation by configurations.creating
val testAgent by configurations.creating

dependencies {
  compileOnly("io.opentelemetry:opentelemetry-sdk")
  compileOnly("io.opentelemetry.instrumentation:opentelemetry-instrumentation-api")
  compileOnly("io.opentelemetry.javaagent:opentelemetry-javaagent-extension-api")

  autoservice.forEach {
    annotationProcessor(it)
    compileOnly(it)
  }

  // the javaagent that is going to be used when running instrumentation unit tests
  testAgent(project(path = ":testing:agent-for-testing", configuration = "shadow"))
  // test dependencies
  testImplementation("io.opentelemetry.javaagent:opentelemetry-testing-common")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing")
  testImplementation("org.assertj:assertj-core:3.27.7")

  add("codegen", "io.opentelemetry.javaagent:opentelemetry-javaagent-tooling:${otelVersions.opentelemetryJavaagentAlpha}")
  add("muzzleBootstrap", "io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations-support:${otelVersions.opentelemetryJavaagentAlpha}")
  add("muzzleTooling", "io.opentelemetry.javaagent:opentelemetry-javaagent-extension-api:${otelVersions.opentelemetryJavaagentAlpha}")
  add("muzzleTooling", "io.opentelemetry.javaagent:opentelemetry-javaagent-tooling:${otelVersions.opentelemetryJavaagentAlpha}")

  compileOnly(project(":bootstrap"))
  compileOnly("javax.servlet:javax.servlet-api:3.0.1")

  testInstrumentation("io.opentelemetry.javaagent.instrumentation:opentelemetry-javaagent-servlet-common:${otelVersions.opentelemetryJavaagentAlpha}")
  testInstrumentation("io.opentelemetry.javaagent.instrumentation:opentelemetry-javaagent-servlet-2.2:${otelVersions.opentelemetryJavaagentAlpha}")
  testInstrumentation("io.opentelemetry.javaagent.instrumentation:opentelemetry-javaagent-servlet-3.0:${otelVersions.opentelemetryJavaagentAlpha}")

  testImplementation("io.opentelemetry.javaagent:opentelemetry-testing-common") {
    exclude(group = "org.eclipse.jetty", module = "jetty-server")
  }

  testImplementation("com.squareup.okhttp3:okhttp:5.3.2")
  testImplementation("javax.servlet:javax.servlet-api:3.0.1")
  testImplementation("org.eclipse.jetty:jetty-server:8.2.0.v20160908")
  testImplementation("org.eclipse.jetty:jetty-servlet:8.2.0.v20160908")
}

muzzle {
  pass {
    group.set("javax.servlet")
    module.set("javax.servlet-api")
    versions.set("[3.0,)")
    assertInverse.set(true)
  }
  pass {
    group.set("javax.servlet")
    module.set("servlet-api")
    versions.set("[2.2, 3.0)")
    assertInverse.set(true)
  }
}

tasks.shadowJar {
  configurations = listOf(project.configurations.runtimeClasspath.get(), testInstrumentation)

  mergeServiceFiles()
  // mergeServiceFiles requires that duplicate strategy is set to include
  filesMatching("META-INF/services/**") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
  }

  archiveFileName.set("agent-testing.jar")

  relocatePackages(this)
}

tasks.withType<Test>().configureEach {
  val shadowJar = tasks.shadowJar
  inputs.file(shadowJar.flatMap { it.archiveFile })

  jvmArgs("-Dotel.javaagent.debug=true")
  jvmArgs("-Dotel.javaagent.experimental.initializer.jar=${shadowJar.get().archiveFile.get().asFile.absolutePath}")
  jvmArgs("-Dotel.javaagent.testing.additional-library-ignores.enabled=false")
  jvmArgs("-Dotel.javaagent.testing.fail-on-context-leak=true")
  // prevent sporadic gradle deadlocks, see SafeLogger for more details
  jvmArgs("-Dotel.javaagent.testing.transform-safe-logging.enabled=true")

  jvmArgumentProviders.add(JavaagentProvider(project.providers.provider {
    testAgent.files.first()
  }))

  dependsOn(shadowJar)
  dependsOn(testAgent.buildDependencies)

  // The sources are packaged into the testing jar so we need to make sure to exclude from the test
  // classpath, which automatically inherits them, to ensure our shaded versions are used.
  val resourcesMain = layout.buildDirectory.dir("resources/main").get().asFile
  val classesMain = layout.buildDirectory.dir("classes/java/main").get().asFile
  classpath = classpath.filter {
    it != resourcesMain && it != classesMain
  }
}

class JavaagentProvider(
  @get:InputFile
  @get:PathSensitive(PathSensitivity.RELATIVE)
  val agentJar: Provider<File>,
) : CommandLineArgumentProvider {
  override fun asArguments(): Iterable<String> = listOf("-javaagent:${agentJar.get().absolutePath}")
}
