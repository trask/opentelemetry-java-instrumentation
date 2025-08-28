plugins {
  id("otel.javaagent-instrumentation")
}

muzzle {
  pass {
    group.set("org.apache.kafka")
    module.set("kafka-clients")
    versions.set("[0.11.0.0,)")
    assertInverse.set(true)
    excludeInstrumentationName("kafka-clients-metrics")
  }
}

dependencies {
  compileOnly("com.google.auto.value:auto-value-annotations")
  annotationProcessor("com.google.auto.value:auto-value")

  bootstrap(project(":instrumentation:kafka:kafka-clients:kafka-clients-0.11:bootstrap"))
  implementation(project(":instrumentation:kafka:kafka-clients:kafka-clients-common-0.11:library"))

  library("org.apache.kafka:kafka-clients:0.11.0.0")

  testImplementation("org.testcontainers:kafka")
  testImplementation(project(":instrumentation:kafka:kafka-clients:kafka-clients-0.11:testing"))
}
testing {
  suites {
    // Configure the default test suite
    named<JvmTestSuite>("test") {
      targets {
        all {
          testTask.configure {
            filter {
                  excludeTestsMatching("KafkaClientPropagationDisabledTest")
                  excludeTestsMatching("KafkaClientSuppressReceiveSpansTest")
          }
        }
      }
    }
    
    val testPropagationDisabled by registering(JvmTestSuite::class) {
      targets {
        all {
          testTask.configure {
            filter {
                  includeTestsMatching("KafkaClientPropagationDisabledTest")
          }
        }
      }
    }
    val testReceiveSpansDisabled by registering(JvmTestSuite::class) {
      targets {
        all {
          testTask.configure {
            filter {
                  includeTestsMatching("KafkaClientSuppressReceiveSpansTest")
          }
        }
      }
    }
  }
}



tasks {
  withType<Test>().configureEach {
    usesService(gradle.sharedServices.registrations["testcontainersBuildService"].service)

    systemProperty("testLatestDeps", findProperty("testLatestDeps") as Boolean)

    // TODO run tests both with and without experimental span attributes
    jvmArgs("-Dotel.instrumentation.kafka.experimental-span-attributes=true")
  }
    include("**/KafkaClientPropagationDisabledTest.*")
    jvmArgs("-Dotel.instrumentation.kafka.producer-propagation.enabled=false")
  }
    include("**/KafkaClientSuppressReceiveSpansTest.*")
  }
    jvmArgs("-Dotel.instrumentation.messaging.experimental.receive-telemetry.enabled=true")
  }

  check {
    dependsOn(testing.suites.named("testPropagationDisabled"))
    dependsOn(testing.suites.named("testReceiveSpansDisabled"))
  }
}
