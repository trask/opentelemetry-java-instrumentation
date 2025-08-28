/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.testing.junit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test Issue #70 - This is a simple test to verify permissions are working correctly.
 * This test can be safely deleted after verification is complete.
 */
class PermissionVerificationTest {

  @Test
  void verifyPermissionsWork() {
    // Simple test to verify we can create and run tests
    assertTrue(true, "Permissions verification test should always pass");
  }

  @Test
  void verifyBasicFunctionality() {
    // Verify basic Java functionality works
    String testString = "test-issue-70";
    assertTrue(testString.contains("test"), "String should contain 'test'");
    assertTrue(testString.contains("70"), "String should contain '70'");
  }
}