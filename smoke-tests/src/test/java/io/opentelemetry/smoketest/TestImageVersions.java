/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.smoketest;

/**
 * Image tag used for locally-built smoke test images. All smoke test images are built on-demand
 * before running smoke tests, so they use a consistent "local" tag.
 */
public final class TestImageVersions {

  public static final String IMAGE_TAG = "local";

  private TestImageVersions() {}
}
