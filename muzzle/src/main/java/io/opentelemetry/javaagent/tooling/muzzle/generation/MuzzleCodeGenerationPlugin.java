/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.tooling.muzzle.generation;

import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;

/**
 * This class is a ByteBuddy build plugin that is responsible for generating actual implementation
 * of some {@link InstrumentationModule} methods. Auto-generated methods have the word "muzzle" in
 * their names.
 *
 * <p>This class is used in the gradle build scripts, referenced by each instrumentation module.
 *
 * <p>This plugin can be used in three ways:
 *
 * <ul>
 *   <li>With the Byte Buddy Maven plugin, which provides the project's compile classpath as a
 *       {@code File[]} constructor argument via its built-in argument resolution.
 *   <li>With the custom OpenTelemetry Gradle plugin infrastructure, which provides an explicit
 *       {@link URLClassLoader} containing the full classpath.
 *   <li>Via the default no-arg constructor, which derives the classpath from the class loader that
 *       loaded this plugin class.
 * </ul>
 */
public final class MuzzleCodeGenerationPlugin implements Plugin {

  private static final TypeDescription instrumentationModuleType =
      new TypeDescription.ForLoadedType(InstrumentationModule.class);

  private final URLClassLoader classLoader;

  /**
   * Constructor for use with the Byte Buddy Maven plugin. The Maven plugin provides the project's
   * compile classpath as a {@code File[]} via its built-in {@link
   * Plugin.Factory.UsingReflection.ArgumentResolver}. This constructor combines those classpath
   * elements with the plugin's own class loader URLs to create a single {@link URLClassLoader}
   * containing both the project's classes and the instrumentation tooling dependencies.
   *
   * @param classPath the project's compile classpath elements, provided by the Byte Buddy Maven
   *     plugin
   * @throws IllegalStateException if the class loader that loaded this plugin is not a {@link
   *     URLClassLoader}
   */
  public MuzzleCodeGenerationPlugin(File[] classPath) {
    ClassLoader pluginClassLoader = getClass().getClassLoader();
    if (!(pluginClassLoader instanceof URLClassLoader)) {
      throw new IllegalStateException(
          "MuzzleCodeGenerationPlugin must be loaded from a URLClassLoader, but was loaded from "
              + (pluginClassLoader == null
                  ? "bootstrap class loader"
                  : pluginClassLoader.getClass().getName()));
    }

    // Combine the plugin's own dependencies (OTel tooling, etc.) with the project's compile
    // classpath (project classes + compile dependencies) into a single class loader.
    // The plugin class loader is used as parent so that classes like InstrumentationModule
    // resolve to the same instance used by MuzzleCodeGenerator (which was loaded by the plugin
    // class loader). The plugin URLs are also added to our own URLs so that getURLs() returns
    // everything — MuzzleCodeGenerator.collectReferences() uses getURLs() to create an isolated
    // resource loader with a null parent.
    URL[] pluginUrls = ((URLClassLoader) pluginClassLoader).getURLs();
    List<URL> allUrls = new ArrayList<>(pluginUrls.length + classPath.length);
    Collections.addAll(allUrls, pluginUrls);
    for (File file : classPath) {
      try {
        allUrls.add(file.toURI().toURL());
      } catch (MalformedURLException e) {
        throw new IllegalStateException("Cannot convert classpath element to URL: " + file, e);
      }
    }
    this.classLoader = new URLClassLoader(allUrls.toArray(new URL[0]), pluginClassLoader);
  }

  /**
   * Default constructor for use with standard Byte Buddy build plugins. The classpath is derived
   * from the class loader that loaded this plugin class.
   *
   * @throws IllegalStateException if the class loader that loaded this plugin is not a {@link
   *     URLClassLoader}
   */
  public MuzzleCodeGenerationPlugin() {
    ClassLoader cl = getClass().getClassLoader();
    if (!(cl instanceof URLClassLoader)) {
      throw new IllegalStateException(
          "MuzzleCodeGenerationPlugin must be loaded from a URLClassLoader, but was loaded from "
              + (cl == null ? "bootstrap class loader" : cl.getClass().getName())
              + ". Use the Byte Buddy Maven or Gradle plugin, or pass a URLClassLoader explicitly.");
    }
    this.classLoader = (URLClassLoader) cl;
  }

  /**
   * Constructor for use with custom build infrastructure (e.g., the OpenTelemetry Gradle plugin)
   * that provides an explicit classpath.
   */
  public MuzzleCodeGenerationPlugin(URLClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  public boolean matches(TypeDescription target) {
    if (target.isAbstract()) {
      return false;
    }
    boolean isInstrumentationModule = false;
    TypeDefinition instrumentation = target.getSuperClass();
    while (instrumentation != null) {
      if (instrumentation.equals(instrumentationModuleType)) {
        isInstrumentationModule = true;
        break;
      }
      instrumentation = instrumentation.getSuperClass();
    }
    return isInstrumentationModule;
  }

  @Override
  public DynamicType.Builder<?> apply(
      DynamicType.Builder<?> builder,
      TypeDescription typeDescription,
      ClassFileLocator classFileLocator) {
    return builder.visit(new MuzzleCodeGenerator(classLoader));
  }

  @Override
  public void close() {}
}
