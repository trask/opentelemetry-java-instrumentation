plugins {
  id("otel.javaagent-testing")
}

dependencies {
  compileOnly(project(":testing-common:library-for-integration-tests"))
  testImplementation(project(":testing-common:library-for-integration-tests"))

  testCompileOnly(project(":instrumentation-api"))
  testCompileOnly(project(":javaagent-tooling"))
  testCompileOnly(project(":javaagent-bootstrap"))
  testCompileOnly(project(":javaagent-extension-api"))
  testCompileOnly(project(":muzzle"))
  testCompileOnly("com.google.auto.service:auto-service-annotations")
  testCompileOnly("com.google.code.findbugs:annotations")

  testImplementation("net.bytebuddy:byte-buddy")
  testImplementation("net.bytebuddy:byte-buddy-agent")

  testImplementation("com.google.guava:guava")
  testImplementation(project(":instrumentation-annotations"))

  testImplementation("cglib:cglib:3.3.0")

  // test instrumenting java 1.1 bytecode
  // TODO do we want this?
  testImplementation("net.sf.jt400:jt400:6.1")
}


testing {
  suites {
    val testFieldInjectionDisabled by registering(JvmTestSuite::class) {
      targets {
        all {
          testTask.configure {
            filter {
              includeTestsMatching("context.FieldInjectionDisabledTest")
            }
          }
        }
      }
    }

    val testFieldBackedImplementation by registering(JvmTestSuite::class) {
      targets {
        all {
          testTask.configure {
            filter {
              includeTestsMatching("context.FieldBackedImplementationTest")
            }
          }
        }
      }
    }
  }
}

tasks {
  test {
    filter {
      excludeTestsMatching("context.FieldInjectionDisabledTest")
      excludeTestsMatching("context.FieldBackedImplementationTest")
    }
    // this is needed for AgentInstrumentationSpecificationTest
    jvmArgs("-Dotel.javaagent.exclude-classes=config.exclude.packagename.*,config.exclude.SomeClass,config.exclude.SomeClass\$NestedClass")
  }

  check {
    dependsOn(testing.suites.named("testFieldInjectionDisabled"))
    dependsOn(testing.suites.named("testFieldBackedImplementation"))
  }
}
