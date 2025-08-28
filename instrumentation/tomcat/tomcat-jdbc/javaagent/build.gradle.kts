plugins {
  id("otel.javaagent-instrumentation")
}

muzzle {
  pass {
    group.set("org.apache.tomcat")
    module.set("tomcat-jdbc")
    versions.set("[8.5.0,)")
    // no assertInverse because tomcat-jdbc < 8.5 doesn't have methods that we hook into
  }
}

dependencies {
  compileOnly("org.apache.tomcat:tomcat-jdbc:8.5.0")
  testImplementation("org.apache.tomcat:tomcat-jdbc:8.5.0")
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
