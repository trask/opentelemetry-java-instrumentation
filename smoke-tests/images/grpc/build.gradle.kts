plugins {
  id("otel.java-conventions")
  id("com.gradleup.shadow")
}

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
}

// Compile to Java 8 so the same JAR can be tested on all JDK versions
java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
  shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()

    manifest {
      attributes["Main-Class"] = "io.opentelemetry.smoketest.grpc.TestMain"
    }
  }

  // Expose the shadow JAR for consumption by the smoke tests
  val grpcJar by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
  }

  artifacts {
    add("grpcJar", shadowJar)
  }
}
