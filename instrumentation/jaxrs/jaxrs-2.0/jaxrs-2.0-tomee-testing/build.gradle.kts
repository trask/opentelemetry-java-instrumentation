plugins {
  id("otel.javaagent-testing")
}

// add repo for org.gradle:gradle-tooling-api which org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-gradle-depchain
// which is used by jaxrs-2.0-arquillian-testing depends on
repositories {
  mavenCentral()
  maven {
    setUrl("https://repo.gradle.org/artifactory/libs-releases-local")
    content {
      includeGroup("org.gradle")
    }
  }
  mavenLocal()
}

dependencies {
  testImplementation(project(":instrumentation:jaxrs:jaxrs-2.0:jaxrs-2.0-arquillian-testing"))
  testCompileOnly("jakarta.enterprise:jakarta.enterprise.cdi-api:2.0.2")
  testRuntimeOnly("org.apache.tomee:arquillian-tomee-embedded:8.0.6")
  testRuntimeOnly("org.apache.tomee:tomee-embedded:8.0.6")
  testRuntimeOnly("org.apache.tomee:tomee-jaxrs:8.0.6")

  testInstrumentation(project(":instrumentation:servlet:servlet-3.0:javaagent"))
  testInstrumentation(project(":instrumentation:jaxrs:jaxrs-2.0:jaxrs-2.0-common:javaagent"))
  testInstrumentation(project(":instrumentation:jaxrs:jaxrs-2.0:jaxrs-2.0-cxf-3.2:javaagent"))
}
