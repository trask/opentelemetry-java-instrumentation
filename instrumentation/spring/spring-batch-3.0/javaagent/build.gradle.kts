plugins {
  id("otel.javaagent-instrumentation")
}

muzzle {
  pass {
    group.set("org.springframework.batch")
    module.set("spring-batch-core")
    versions.set("[3.0.0.RELEASE,5)")
    assertInverse.set(true)
  }
}

dependencies {
  library("org.springframework.batch:spring-batch-core:3.0.0.RELEASE")

  testImplementation("com.google.guava:guava")
  testImplementation("javax.inject:javax.inject:1")

  // SimpleAsyncTaskExecutor context propagation
  testInstrumentation(project(":instrumentation:spring:spring-core-2.0:javaagent"))

  // spring batch 5.0 uses spring framework 6.0
  latestDepTestLibrary("org.springframework.batch:spring-batch-core:4.+") // documented limitation
}


testing {
  suites {
    val testChunkRootSpan by registering(JvmTestSuite::class) {
      targets {
        all {
          testTask.configure {
            filter {
      includeTestsMatching("*ChunkRootSpanTest")
          }
        }
      }
    }

    val testItemLevelSpan by registering(JvmTestSuite::class) {
      targets {
        all {
          testTask.configure {
            filter {
      includeTestsMatching("*ItemLevelSpanTest")
      includeTestsMatching("*CustomSpanEventTest")
          }
        }
      }
    }
  }
}

tasks {
  test {
    filter {
      excludeTestsMatching("*ChunkRootSpanTest")
      excludeTestsMatching("*ItemLevelSpanTest")
      excludeTestsMatching("*CustomSpanEventTest")
    }

    systemProperty("collectMetadata", findProperty("collectMetadata")?.toString() ?: "false")
    systemProperty("metadataConfig", "otel.instrumentation.spring-batch.experimental-span-attributes=true")
  }

  check {
    dependsOn(testing.suites.named("testChunkRootSpan"))
    dependsOn(testing.suites.named("testItemLevelSpan"))
  }

  withType<Test>().configureEach {
    systemProperty("testLatestDeps", findProperty("testLatestDeps") as Boolean)
    jvmArgs("-Dotel.instrumentation.spring-batch.enabled=true")
    // TODO run tests both with and without experimental span attributes
    jvmArgs("-Dotel.instrumentation.spring-batch.experimental-span-attributes=true")
  }
}

tasks.withType<Test>().configureEach {
  // required on jdk17
  jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
  jvmArgs("-XX:+IgnoreUnrecognizedVMOptions")
}
