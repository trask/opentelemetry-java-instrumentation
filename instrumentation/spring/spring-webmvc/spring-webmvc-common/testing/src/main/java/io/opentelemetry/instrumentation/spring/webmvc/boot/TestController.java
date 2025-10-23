/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.spring.webmvc.boot;

import static io.opentelemetry.instrumentation.testing.junit.http.AbstractHttpServerTest.controller;
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.CAPTURE_HEADERS;
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.ERROR;
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.EXCEPTION;
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.INDEXED_CHILD;
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.PATH_PARAM;
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.QUERY_PARAM;
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.REDIRECT;
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.SUCCESS;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class TestController {

  @Autowired private TestBean testBean;

  @GetMapping("/basicsecured/endpoint")
  String secureEndpoint() {
    return controller(SUCCESS, SUCCESS::getBody);
  }

  @GetMapping("/success")
  String success() {
    return controller(SUCCESS, SUCCESS::getBody);
  }

  @GetMapping("/query")
  String queryParam(@RequestParam("some") String param) {
    return controller(QUERY_PARAM, () -> "some=" + param);
  }

  @GetMapping("/redirect")
  RedirectView redirect() {
    return controller(REDIRECT, () -> new RedirectView(REDIRECT.getBody()));
  }

  @GetMapping("/error-status")
  ResponseEntity<String> error() {
    return controller(
        ERROR,
        () ->
            ResponseEntity.status(HttpStatus.valueOf(ERROR.getStatus()).value())
                .body(ERROR.getBody()));
  }

  @SuppressWarnings("ThrowSpecificExceptions")
  @GetMapping("/exception")
  ResponseEntity<String> exception() {
    return controller(
        EXCEPTION,
        () -> {
          throw new RuntimeException(EXCEPTION.getBody());
        });
  }

  @GetMapping("/captureHeaders")
  ResponseEntity<String> captureHeaders(@RequestHeader("X-Test-Request") String testRequestHeader) {
    return controller(
        CAPTURE_HEADERS,
        () ->
            ResponseEntity.ok()
                .header("X-Test-Response", testRequestHeader)
                .body(CAPTURE_HEADERS.getBody()));
  }

  @GetMapping("/path/{id}/param")
  String pathParam(@PathVariable("id") int id) {
    return controller(PATH_PARAM, () -> String.valueOf(id));
  }

  @GetMapping("/child")
  String indexedChild(@RequestParam("id") String id) {
    return controller(
        INDEXED_CHILD,
        () -> {
          INDEXED_CHILD.collectSpanAttributes(it -> Objects.equals(it, "id") ? id : null);
          return INDEXED_CHILD.getBody();
        });
  }

  @GetMapping("/deferred-result")
  DeferredResult<String> deferredResult() {
    DeferredResult<String> deferredResult = new DeferredResult<>();
    testBean.asyncDependencyCall(deferredResult);
    return deferredResult;git sta
  }

  @ExceptionHandler
  ResponseEntity<String> handleException(Throwable throwable) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .body(throwable.getMessage());
  }
}
