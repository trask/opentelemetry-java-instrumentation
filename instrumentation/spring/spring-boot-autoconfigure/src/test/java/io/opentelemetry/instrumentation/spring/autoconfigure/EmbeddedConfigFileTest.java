/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.spring.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EmbeddedConfigFileTest {

  @Test
  void convertFlatPropsToNested_simpleProperties() {
    Map<String, String> flatProps = new HashMap<>();
    flatProps.put("resource.service.name", "my-service");
    flatProps.put("traces.exporter", "otlp");

    Map<String, Object> result = EmbeddedConfigFile.convertFlatPropsToNested(flatProps);

    assertThat(result)
        .containsOnlyKeys("resource", "traces")
        .satisfies(
            val -> {
              assertThat(val.get("resource"))
                  .isInstanceOf(Map.class)
                  .satisfies(
                      v -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> resourceMap = (Map<String, Object>) v;
                        assertThat(resourceMap)
                            .containsOnlyKeys("service")
                            .satisfies(
                                v -> {
                                  @SuppressWarnings("unchecked")
                                  Map<String, Object> serviceMap =
                                      (Map<String, Object>) v.get("service");
                                  assertThat(serviceMap).containsEntry("name", "my-service");
                                });
                      });
              assertThat(val.get("traces"))
                  .isInstanceOf(Map.class)
                  .satisfies(
                      v -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> tracesMap = (Map<String, Object>) v;
                        assertThat(tracesMap).containsEntry("exporter", "otlp");
                      });
            });
  }

  @Test
  void convertFlatPropsToNested_arrayProperties() {
    Map<String, String> flatProps = new HashMap<>();
    flatProps.put("instrumentation.java.list[0]", "one");
    flatProps.put("instrumentation.java.list[1]", "two");
    flatProps.put("instrumentation.java.list[2]", "three");

    Map<String, Object> result = EmbeddedConfigFile.convertFlatPropsToNested(flatProps);

    assertThat(result)
        .containsOnlyKeys("instrumentation")
        .satisfies(
            val -> {
              @SuppressWarnings("unchecked")
              Map<String, Object> instrumentation =
                  (Map<String, Object>) val.get("instrumentation");
              assertThat(instrumentation)
                  .containsOnlyKeys("java")
                  .satisfies(
                      val -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> javaMap = (Map<String, Object>) val.get("java");
                        assertThat(javaMap)
                            .containsOnlyKeys("list")
                            .satisfies(
                                val -> {
                                  @SuppressWarnings("unchecked")
                                  List<Object> list = (List<Object>) val.get("list");
                                  assertThat(list).containsExactly("one", "two", "three");
                                });
                      });
            });
  }

  @Test
  void convertFlatPropsToNested_mixedPropertiesAndArrays() {
    Map<String, String> flatProps = new HashMap<>();
    flatProps.put("resource.service.name", "test-service");
    flatProps.put("resource.attributes[0]", "key1=value1");
    flatProps.put("resource.attributes[1]", "key2=value2");
    flatProps.put("traces.exporter", "otlp");

    Map<String, Object> result = EmbeddedConfigFile.convertFlatPropsToNested(flatProps);

    assertThat(result)
        .containsOnlyKeys("resource", "traces")
        .satisfies(
            val -> {
              @SuppressWarnings("unchecked")
              Map<String, Object> resource = (Map<String, Object>) val.get("resource");
              assertThat(resource)
                  .containsKeys("service", "attributes")
                  .satisfies(
                      val -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> service = (Map<String, Object>) val.get("service");
                        assertThat(service).containsEntry("name", "test-service");

                        @SuppressWarnings("unchecked")
                        List<Object> attributes = (List<Object>) val.get("attributes");
                        assertThat(attributes).containsExactly("key1=value1", "key2=value2");
                      });

              @SuppressWarnings("unchecked")
              Map<String, Object> traces = (Map<String, Object>) val.get("traces");
              assertThat(traces).containsEntry("exporter", "otlp");
            });
  }

  @Test
  void convertFlatPropsToNested_emptyMap() {
    Map<String, String> flatProps = new HashMap<>();

    Map<String, Object> result = EmbeddedConfigFile.convertFlatPropsToNested(flatProps);

    assertThat(result).isEmpty();
  }

  @Test
  void convertFlatPropsToNested_singleLevelProperty() {
    Map<String, String> flatProps = new HashMap<>();
    flatProps.put("enabled", "true");

    Map<String, Object> result = EmbeddedConfigFile.convertFlatPropsToNested(flatProps);

    assertThat(result).containsEntry("enabled", "true");
  }

  @Test
  void convertFlatPropsToNested_arrayWithGaps() {
    Map<String, String> flatProps = new HashMap<>();
    flatProps.put("list[0]", "first");
    flatProps.put("list[2]", "third");

    Map<String, Object> result = EmbeddedConfigFile.convertFlatPropsToNested(flatProps);

    assertThat(result)
        .containsOnlyKeys("list")
        .satisfies(
            val -> {
              @SuppressWarnings("unchecked")
              List<Object> list = (List<Object>) val.get("list");
              assertThat(list).hasSize(3).containsExactly("first", null, "third");
            });
  }

  @Test
  void convertFlatPropsToNested_deeplyNestedProperties() {
    Map<String, String> flatProps = new HashMap<>();
    flatProps.put("a.b.c.d.e", "deep-value");

    Map<String, Object> result = EmbeddedConfigFile.convertFlatPropsToNested(flatProps);

    assertThat(result).containsOnlyKeys("a");
    @SuppressWarnings("unchecked")
    Map<String, Object> a = (Map<String, Object>) result.get("a");
    assertThat(a).containsOnlyKeys("b");
    @SuppressWarnings("unchecked")
    Map<String, Object> b = (Map<String, Object>) a.get("b");
    assertThat(b).containsOnlyKeys("c");
    @SuppressWarnings("unchecked")
    Map<String, Object> c = (Map<String, Object>) b.get("c");
    assertThat(c).containsOnlyKeys("d");
    @SuppressWarnings("unchecked")
    Map<String, Object> d = (Map<String, Object>) c.get("d");
    assertThat(d).containsEntry("e", "deep-value");
  }

  @Test
  void convertFlatPropsToNested_nestedArrays() {
    Map<String, String> flatProps = new HashMap<>();
    flatProps.put("outer[0].inner[0]", "value1");
    flatProps.put("outer[0].inner[1]", "value2");
    flatProps.put("outer[1].inner[0]", "value3");

    Map<String, Object> result = EmbeddedConfigFile.convertFlatPropsToNested(flatProps);

    assertThat(result)
        .containsOnlyKeys("outer")
        .satisfies(
            val -> {
              @SuppressWarnings("unchecked")
              List<Object> outer = (List<Object>) val.get("outer");
              assertThat(outer).hasSize(2);

              @SuppressWarnings("unchecked")
              Map<String, Object> firstElement = (Map<String, Object>) outer.get(0);
              @SuppressWarnings("unchecked")
              List<Object> firstInner = (List<Object>) firstElement.get("inner");
              assertThat(firstInner).containsExactly("value1", "value2");

              @SuppressWarnings("unchecked")
              Map<String, Object> secondElement = (Map<String, Object>) outer.get(1);
              @SuppressWarnings("unchecked")
              List<Object> secondInner = (List<Object>) secondElement.get("inner");
              assertThat(secondInner).containsExactly("value3");
            });
  }
}
