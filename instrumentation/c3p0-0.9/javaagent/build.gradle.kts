plugins {
  id("otel.javaagent-instrumentation")
}

muzzle {
  pass {
    group.set("com.mchange")
    module.set("c3p0")
    versions.set("(,)")
    // these versions have missing dependencies in maven central
    skip("0.9.2-pre2-RELEASE", "0.9.2-pre3")
  }
}

dependencies {
  // first non pre-release version available on maven central
  library("com.mchange:c3p0:0.9.2")

  implementation(project(":instrumentation:c3p0-0.9:library"))

  testImplementation(project(":instrumentation:c3p0-0.9:testing"))
}
testing {
  suites {
    // Configure the default test suite
    named<JvmTestSuite>("test") {
      targets {
        all {
          testTask.configure {
            systemProperty("collectMetadata", collectMetadata)
          }
        }
      }
    }
    
    val testStableSemconv by registering(JvmTestSuite::class) {
      targets {
        all {
          testTask.configure {
            jvmArgs("-Dotel.semconv-stability.opt-in=database")

                systemProperty("collectMetadata", collectMetadata)
                systemProperty("metadataConfig", "otel.semconv-stability.opt-in=database")
          }
        }
      }
    }
  }
}



val collectMetadata = findProperty("collectMetadata")?.toString() ?: "false"

tasks {

  check {
    dependsOn(testing.suites.named("testStableSemconv"))
  }
}
