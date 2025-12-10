plugins {
  id("otel.java-conventions")

  id("com.gradleup.shadow")
  application
}

dependencies {
  implementation(platform("io.opentelemetry:opentelemetry-bom:1.0.0"))

  implementation("io.opentelemetry:opentelemetry-api")
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

application {
  mainClass.set("io.opentelemetry.smoketest.securitymanager.Main")
}
