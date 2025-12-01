/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.spring.webflux.v5_0.server.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {

  @GetMapping("/secured/test")
  public Mono<String> securedEndpoint() {
    return Mono.just("secured");
  }

  @GetMapping("/unsecured/test")
  public Mono<String> unsecuredEndpoint() {
    return Mono.just("unsecured");
  }
}
