# Maven Extension Example

This is a minimal example showing how to build an OpenTelemetry javaagent extension using **Maven**
and the standard [Byte Buddy Maven plugin](https://bytebuddy.net/#/tutorial-maven) for muzzle code
generation.

## What this demonstrates

The `MuzzleCodeGenerationPlugin` is a standard Byte Buddy plugin — it has a public default
constructor and can be used directly with the `byte-buddy-maven-plugin`. No custom build
infrastructure is required.

## How it works

The `byte-buddy-maven-plugin` is configured to run `MuzzleCodeGenerationPlugin` as a post-compile
transformation. The plugin generates muzzle reference-checking methods on `InstrumentationModule`
subclasses, which the javaagent uses at runtime to verify that the instrumented library version is
compatible.

## Building

```shell
mvn package
```

## Key Maven configuration

In your `pom.xml`, configure the Byte Buddy Maven plugin with `MuzzleCodeGenerationPlugin` and add
`opentelemetry-javaagent-tooling` as a plugin dependency so the plugin class can be loaded:

```xml
<plugin>
  <groupId>net.bytebuddy</groupId>
  <artifactId>byte-buddy-maven-plugin</artifactId>
  <version>${byte-buddy.version}</version>
  <executions>
    <execution>
      <goals>
        <goal>transform</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <classFileVersion>JAVA_V8</classFileVersion>
    <transformations>
      <transformation>
        <plugin>io.opentelemetry.javaagent.tooling.muzzle.generation.MuzzleCodeGenerationPlugin</plugin>
      </transformation>
    </transformations>
  </configuration>
  <dependencies>
    <dependency>
      <groupId>io.opentelemetry.javaagent</groupId>
      <artifactId>opentelemetry-javaagent-tooling</artifactId>
      <version>${opentelemetry.javaagent.alpha.version}</version>
    </dependency>
  </dependencies>
</plugin>
```
