plugins {
  id("otel.javaagent-instrumentation")
}

muzzle {
  pass {
    group.set("org.apache.logging.log4j")
    module.set("log4j-core")
    versions.set("[2.7,2.17.0)")
    assertInverse.set(true)
  }
}

dependencies {
  library("org.apache.logging.log4j:log4j-core:2.7")

  testInstrumentation(project(":instrumentation:log4j:log4j-context-data:log4j-context-data-2.17:javaagent"))

  testImplementation(project(":instrumentation:log4j:log4j-context-data:log4j-context-data-common:testing"))

  latestDepTestLibrary("org.apache.logging.log4j:log4j-core:2.16.+") // see log4j-context-data-2.17 module
}


testing {
  suites {
    val testAddBaggage by registering(JvmTestSuite::class) {
      targets {
        all {
          testTask.configure {
            filter {
      includeTestsMatching("Log4j27BaggageTest")
          }
        }
      }
    }

    val testLoggingKeys by registering(JvmTestSuite::class) {
      targets {
        all {
          testTask.configure {
            filter {
      includeTestsMatching("Log4j27LoggingKeysTest")
          }
        }
      }
    }
  }
}

tasks {
  test {
    filter {
      excludeTestsMatching("Log4j27BaggageTest")
      excludeTestsMatching("Log4j27LoggingKeysTest")
    }
    jvmArgs("-Dotel.instrumentation.common.mdc.resource-attributes=service.name,telemetry.sdk.language")
  }

  named("check") {
    dependsOn(testing.suites.named("testAddBaggage"))
    dependsOn(testing.suites.named("testLoggingKeys"))
  }
}
