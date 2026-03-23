/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package rmi.app;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A serializable probe that records when it is deserialized. Used to prove that {@code
 * readObject()} on the RMI transport stream accepts arbitrary classes — not just {@code
 * HashMap<String,String>} — demonstrating the deserialization vulnerability.
 *
 * <p>In a real attack, this would be replaced with a gadget chain (e.g. Commons Collections {@code
 * Transformer} chain) that achieves remote code execution.
 */
public class DeserializationProbe implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Set to {@code true} when any instance of this class is deserialized via {@code readObject}. */
  public static final AtomicBoolean wasDeserialized = new AtomicBoolean(false);

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    wasDeserialized.set(true);
  }
}
