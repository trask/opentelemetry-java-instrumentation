plugins {
  id("otel.javaagent-instrumentation")
}

muzzle {
  pass {
    group.set("org.apache.rocketmq")
    module.set("rocketmq-client-java")
    versions.set("[5.0.0,)")
    assertInverse.set(true)
  }
}

dependencies {
  library("org.apache.rocketmq:rocketmq-client-java:5.0.0")

  testImplementation(project(":instrumentation:rocketmq:rocketmq-client:rocketmq-client-5.0:testing"))
}
testing {
  suites {
    // Configure the default test suite
    named<JvmTestSuite>("test") {
      targets {
        all {
          testTask.configure {
            filter {
                  excludeTestsMatching("RocketMqClientSuppressReceiveSpanTest")
          }
        }
      }
    }
    
    val testReceiveSpanDisabled by registering(JvmTestSuite::class) {
      targets {
        all {
          testTask.configure {
            filter {
                  includeTestsMatching("RocketMqClientSuppressReceiveSpanTest")
          }
        }
      }
    }
  }
}



tasks {
    include("**/RocketMqClientSuppressReceiveSpanTest.*")
  }
    jvmArgs("-Dotel.instrumentation.messaging.experimental.receive-telemetry.enabled=true")
    jvmArgs("-Dotel.instrumentation.common.experimental.controller-telemetry.enabled=true")
  }

  check {
    dependsOn(testing.suites.named("testReceiveSpanDisabled"))
  }
}
