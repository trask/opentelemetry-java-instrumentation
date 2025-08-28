plugins {
  id("otel.javaagent-instrumentation")
}

muzzle {
  pass {
    group.set("jakarta.jms")
    module.set("jakarta.jms-api")
    versions.set("[3.0.0,)")
    assertInverse.set(true)
  }
  fail {
    group.set("javax.jms")
    module.set("jms-api")
    versions.set("(,)")
  }
  fail {
    group.set("javax.jms")
    module.set("javax.jms-api")
    versions.set("(,)")
  }
}

dependencies {
  implementation(project(":instrumentation:jms:jms-common:javaagent"))

  library("jakarta.jms:jakarta.jms-api:3.0.0")

  testImplementation("org.apache.activemq:artemis-jakarta-client:2.27.1")
}
testing {
  suites {
    // Configure the default test suite
    named<JvmTestSuite>("test") {
      targets {
        all {
          testTask.configure {
            usesService(gradle.sharedServices.registrations["testcontainersBuildService"].service)
          }
        }
      }
    }
    
    val testReceiveSpansDisabled by registering(JvmTestSuite::class) {
      targets {
        all {
          testTask.configure {
            filter {
                  includeTestsMatching("Jms3SuppressReceiveSpansTest")
          }
        }
      }
    }
  }
}



otelJava {
  minJavaVersionSupported.set(JavaVersion.VERSION_11)
}

tasks {
    include("**/Jms3SuppressReceiveSpansTest.*")
  }

  test {
    filter {
      excludeTestsMatching("Jms3SuppressReceiveSpansTest")
    }
    jvmArgs("-Dotel.instrumentation.messaging.experimental.receive-telemetry.enabled=true")
  }

  check {
    dependsOn(testing.suites.named("testReceiveSpansDisabled"))
  }
}
