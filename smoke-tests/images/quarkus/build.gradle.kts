plugins {
  // otel.java-conventions isn't applied to this module because it adds
  // platform(project(":dependencyManagement")) which conflicts with
  // Quarkus's dependency resolution logic, causing infinite recursion in
  // GradleApplicationModelBuilder.collectDependencies during the configuration phase
  // resulting in StackOverflowError
  id("java")

  id("io.quarkus") version "3.30.3"
}

dependencies {
  implementation(enforcedPlatform("io.quarkus:quarkus-bom:3.30.3"))
  implementation("io.quarkus:quarkus-rest")
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
}
