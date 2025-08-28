plugins {
  id("otel.javaagent-instrumentation")
}

muzzle {
  pass {
    group.set("org.vibur")
    module.set("vibur-dbcp")
    versions.set("[11.0,)")
    assertInverse.set(true)
  }
}

dependencies {
  library("org.vibur:vibur-dbcp:11.0")

  implementation(project(":instrumentation:vibur-dbcp-11.0:library"))

  testImplementation(project(":instrumentation:vibur-dbcp-11.0:testing"))
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
