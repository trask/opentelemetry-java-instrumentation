import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
  id("com.diffplug.spotless") apply false
}

group = "io.opentelemetry.example"
version = "1.0-SNAPSHOT"

subprojects {
  version = rootProject.version

  apply(plugin = "java")
  apply(plugin = "com.diffplug.spotless")

  extra["versions"] = mapOf(
    // this line is managed by .github/scripts/update-sdk-version.sh
    "opentelemetrySdk" to "1.61.0",

    // these lines are managed by .github/scripts/update-version.sh
    "opentelemetryJavaagent" to "2.28.0-SNAPSHOT",
    "opentelemetryJavaagentAlpha" to "2.28.0-alpha-SNAPSHOT",

    "autoservice" to "1.1.1",
  )

  @Suppress("UNCHECKED_CAST")
  val versions = extra["versions"] as Map<String, String>

  extra["deps"] = mapOf(
    "autoservice" to listOf(
      "com.google.auto.service:auto-service:${versions["autoservice"]}",
      "com.google.auto.service:auto-service-annotations:${versions["autoservice"]}",
    ),
  )

  repositories {
    mavenCentral()
    maven {
      name = "sonatype"
      url = uri("https://central.sonatype.com/repository/maven-snapshots/")
    }
  }

  configure<SpotlessExtension> {
    java {
      googleJavaFormat()
      licenseHeaderFile(rootProject.file("../../buildscripts/spotless.license.java"), "(package|import|public)")
      target("src/**/*.java")
    }
  }

  dependencies {
    "implementation"(platform("io.opentelemetry:opentelemetry-bom:${versions["opentelemetrySdk"]}"))

    // these serve as a test of the instrumentation boms
    "implementation"(platform("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:${versions["opentelemetryJavaagent"]}"))
    "implementation"(platform("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha:${versions["opentelemetryJavaagentAlpha"]}"))

    "testImplementation"("org.mockito:mockito-core:5.23.0")

    "testImplementation"(enforcedPlatform("org.junit:junit-bom:5.14.4"))
    "testImplementation"("org.junit.jupiter:junit-jupiter-api")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
  }

  tasks {
    named<Test>("test") {
      useJUnitPlatform()
    }

    named<JavaCompile>("compileJava") {
      options.release.set(8)
    }
  }
}
