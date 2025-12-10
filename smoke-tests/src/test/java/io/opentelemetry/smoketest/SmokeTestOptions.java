/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.smoketest;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;

public class SmokeTestOptions<T> {

  Function<T, String> getImage;
  String[] command;
  String jvmArgsEnvVarName = "JAVA_TOOL_OPTIONS";
  boolean setServiceName = true;
  final Map<String, String> extraEnv = new HashMap<>();
  List<ResourceMapping> extraResources = List.of();
  TargetWaitStrategy waitStrategy;
  List<Integer> extraPorts = List.of();
  Duration telemetryTimeout = Duration.ofSeconds(30);
  // Path to app JAR to copy into container (alternative to pre-built image)
  @Nullable String appJarPath;
  @Nullable String appJarContainerPath;
  // Path to app directory to copy into container (for distribution-style apps like Play)
  @Nullable String appDirPath;
  @Nullable String appDirContainerPath;

  /** Sets the container image to run. */
  @CanIgnoreReturnValue
  public SmokeTestOptions<T> image(Function<T, String> getImage) {
    this.getImage = getImage;
    return this;
  }

  /**
   * Configure test to use a base JDK image with an app JAR copied in. This avoids needing to
   * pre-build Docker images.
   */
  @CanIgnoreReturnValue
  public SmokeTestOptions<T> baseImageWithAppJar(
      Function<T, String> baseImage, String appJarPath, String containerPath, String... command) {
    this.getImage = baseImage;
    this.appJarPath = appJarPath;
    this.appJarContainerPath = containerPath;
    this.command = command;
    return this;
  }

  /** Configure test for spring boot test app. */
  @CanIgnoreReturnValue
  public SmokeTestOptions<T> springBoot() {
    // Use base JDK image and copy the spring boot JAR at runtime
    String jarPath = System.getProperty("io.opentelemetry.smoketest.springboot.shadowJar.path");
    if (jarPath != null) {
      baseImageWithAppJar(
          jdk -> "eclipse-temurin:" + jdk,
          jarPath,
          "/app.jar",
          "java", "-jar", "/app.jar");
    } else {
      // Fall back to pre-built image if JAR path not provided
      image(
          jdk ->
              String.format(
                  "smoke-test-spring-boot:jdk%s-%s", jdk, TestImageVersions.IMAGE_TAG));
    }
    waitStrategy(
        new TargetWaitStrategy.Log(Duration.ofMinutes(1), ".*Started SpringbootApplication in.*"));
    return this;
  }

  /** Configure test for grpc test app. */
  @CanIgnoreReturnValue
  public SmokeTestOptions<T> grpc() {
    // Use base JDK image and copy the grpc JAR at runtime
    String jarPath = System.getProperty("io.opentelemetry.smoketest.grpc.shadowJar.path");
    if (jarPath != null) {
      baseImageWithAppJar(
          jdk -> "eclipse-temurin:" + jdk, jarPath, "/app.jar", "java", "-jar", "/app.jar");
    } else {
      // Fall back to pre-built image if JAR path not provided
      image(jdk -> String.format("smoke-test-grpc:jdk%s-%s", jdk, TestImageVersions.IMAGE_TAG));
    }
    waitStrategy(new TargetWaitStrategy.Log(Duration.ofMinutes(1), ".*Server started.*"));
    return this;
  }

  /** Configure test for security-manager test app. */
  @CanIgnoreReturnValue
  public SmokeTestOptions<T> securityManager() {
    // Use base JDK image and copy the security-manager JAR at runtime
    String jarPath = System.getProperty("io.opentelemetry.smoketest.securitymanager.shadowJar.path");
    String policyPath =
        System.getProperty("io.opentelemetry.smoketest.securitymanager.securityPolicy.path");
    if (jarPath != null && policyPath != null) {
      baseImageWithAppJar(
          jdk -> "eclipse-temurin:" + jdk,
          jarPath,
          "/app.jar",
          "java",
          "-Djava.security.manager",
          "-Djava.security.policy=/security.policy",
          "-jar",
          "/app.jar");
      extraResources(ResourceMapping.of(policyPath, "/security.policy"));
    } else {
      // Fall back to pre-built image if paths not provided
      image(
          jdk ->
              String.format(
                  "smoke-test-security-manager:jdk%s-%s", jdk, TestImageVersions.IMAGE_TAG));
    }
    env("OTEL_JAVAAGENT_EXPERIMENTAL_SECURITY_MANAGER_SUPPORT_ENABLED", "true");
    return this;
  }

  /** Configure test for Play framework test app. */
  @CanIgnoreReturnValue
  public SmokeTestOptions<T> play() {
    // Use base JDK image and copy the Play distribution directory at runtime
    String distPath = System.getProperty("io.opentelemetry.smoketest.play.dist.path");
    if (distPath != null) {
      this.getImage = jdk -> "eclipse-temurin:" + jdk;
      this.appDirPath = distPath;
      this.appDirContainerPath = "/app";
      this.command = new String[] {"/app/bin/play"};
    } else {
      // Fall back to pre-built image if path not provided
      image(jdk -> String.format("smoke-test-play:jdk%s-%s", jdk, TestImageVersions.IMAGE_TAG));
    }
    waitStrategy(new TargetWaitStrategy.Log(Duration.ofMinutes(1), ".*Listening for HTTP.*"));
    return this;
  }

  /** Configure test for Quarkus framework test app. */
  @CanIgnoreReturnValue
  public SmokeTestOptions<T> quarkus() {
    // Use base JDK image and copy the Quarkus fast-jar directory at runtime
    String distPath = System.getProperty("io.opentelemetry.smoketest.quarkus.dist.path");
    if (distPath != null) {
      this.getImage = jdk -> "eclipse-temurin:" + jdk;
      this.appDirPath = distPath;
      this.appDirContainerPath = "/app";
      this.command = new String[] {"java", "-jar", "/app/quarkus-run.jar"};
    } else {
      // Fall back to pre-built image if path not provided
      image(jdk -> String.format("smoke-test-quarkus:jdk%s-%s", jdk, TestImageVersions.IMAGE_TAG));
    }
    waitStrategy(new TargetWaitStrategy.Log(Duration.ofMinutes(1), ".*Listening on.*"));
    setServiceName(false);
    return this;
  }

  /** Sets the command to run in the target container. */
  @CanIgnoreReturnValue
  public SmokeTestOptions<T> command(String... command) {
    this.command = command;
    return this;
  }

  /** Sets the environment variable name used to pass JVM arguments to the target application. */
  @CanIgnoreReturnValue
  public SmokeTestOptions<T> jvmArgsEnvVarName(String jvmArgsEnvVarName) {
    this.jvmArgsEnvVarName = jvmArgsEnvVarName;
    return this;
  }

  /** Enables or disables setting the default service name for the target application. */
  @CanIgnoreReturnValue
  public SmokeTestOptions<T> setServiceName(boolean setServiceName) {
    this.setServiceName = setServiceName;
    return this;
  }

  /** Adds an environment variable to the target application's environment. */
  @CanIgnoreReturnValue
  public SmokeTestOptions<T> env(String key, String value) {
    this.extraEnv.put(key, value);
    return this;
  }

  /** Specifies additional files to copy to the target container. */
  @CanIgnoreReturnValue
  public SmokeTestOptions<T> extraResources(ResourceMapping... resources) {
    this.extraResources = List.of(resources);
    return this;
  }

  /** Sets the wait strategy for the target container startup. */
  @CanIgnoreReturnValue
  public SmokeTestOptions<T> waitStrategy(@Nullable TargetWaitStrategy waitStrategy) {
    this.waitStrategy = waitStrategy;
    return this;
  }

  /** Specifies additional ports to expose from the target container. */
  @CanIgnoreReturnValue
  public SmokeTestOptions<T> extraPorts(Integer... ports) {
    this.extraPorts = List.of(ports);
    return this;
  }

  /** Sets the timeout duration for retrieving telemetry data. */
  @CanIgnoreReturnValue
  public SmokeTestOptions<T> telemetryTimeout(Duration telemetryTimeout) {
    this.telemetryTimeout = telemetryTimeout;
    return this;
  }
}
