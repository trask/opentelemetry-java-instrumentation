plugins {
  id("otel.library-instrumentation")
}

base.archivesName.set("${base.archivesName.get()}-autoconfigure")

dependencies {
  compileOnly(project(":javaagent-extension-api"))
  library("org.apache.logging.log4j:log4j-core:2.17.0")

  testImplementation(project(":instrumentation:log4j:log4j-context-data:log4j-context-data-common:testing"))
}


testing {
  suites {
    val testAddBaggage by registering(JvmTestSuite::class) {
      targets {
        all {
          testTask.configure {
            filter {
      includeTestsMatching("LibraryLog4j2BaggageTest")
          }
        }
      }
    }

    val testLoggingKeys by registering(JvmTestSuite::class) {
      targets {
        all {
          testTask.configure {
            filter {
      includeTestsMatching("LibraryLog4j2LoggingKeysTest")
          }
        }
      }
    }
  }
}

tasks {
  test {
    filter {
      excludeTestsMatching("LibraryLog4j2BaggageTest")
      excludeTestsMatching("LibraryLog4j2LoggingKeysTest")
    }
  }

  named("check") {
    dependsOn(testing.suites.named("testAddBaggage"))
    dependsOn(testing.suites.named("testLoggingKeys"))
  }
}
