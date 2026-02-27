/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.grpc.v1_6;

import static java.util.Collections.emptyList;

import io.grpc.ClientInterceptor;
import io.grpc.Context;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.instrumentation.api.incubator.config.internal.DeclarativeConfigUtil;
import io.opentelemetry.instrumentation.api.util.VirtualField;
import io.opentelemetry.instrumentation.grpc.v1_6.GrpcTelemetry;
import io.opentelemetry.instrumentation.grpc.v1_6.internal.ContextStorageBridge;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

// Holds singleton references.
public final class GrpcSingletons {

  public static final VirtualField<ManagedChannelBuilder<?>, Boolean>
      MANAGED_CHANNEL_BUILDER_INSTRUMENTED =
          VirtualField.find(ManagedChannelBuilder.class, Boolean.class);

  public static final VirtualField<ServerBuilder<?>, Boolean> SERVER_BUILDER_INSTRUMENTED =
      VirtualField.find(ServerBuilder.class, Boolean.class);

  public static final ClientInterceptor CLIENT_INTERCEPTOR;

  public static final ServerInterceptor SERVER_INTERCEPTOR;

  private static final AtomicReference<Context.Storage> STORAGE_REFERENCE = new AtomicReference<>();

  static {
    DeclarativeConfigProperties config =
        DeclarativeConfigUtil.getInstrumentationConfig(GlobalOpenTelemetry.get(), "grpc");
    boolean emitMessageEvents = config.getBoolean("emit_message_events", true);

    boolean experimentalSpanAttributes =
        config.getBoolean("experimental_span_attributes/development", false);

    List<String> clientRequestMetadata =
        config
            .get("capture_metadata")
            .get("client")
            .getScalarList("request", String.class, emptyList());
    List<String> serverRequestMetadata =
        config
            .get("capture_metadata")
            .get("server")
            .getScalarList("request", String.class, emptyList());

    GrpcTelemetry telemetry =
        GrpcTelemetry.builder(GlobalOpenTelemetry.get())
            .setEmitMessageEvents(emitMessageEvents)
            .setCaptureExperimentalSpanAttributes(experimentalSpanAttributes)
            .setCapturedClientRequestMetadata(clientRequestMetadata)
            .setCapturedServerRequestMetadata(serverRequestMetadata)
            .build();

    CLIENT_INTERCEPTOR = telemetry.createClientInterceptor();
    SERVER_INTERCEPTOR = telemetry.createServerInterceptor();
  }

  public static Context.Storage getStorage() {
    return STORAGE_REFERENCE.get();
  }

  public static Context.Storage setStorage(Context.Storage storage) {
    // If the original storage is our own ContextStorageOverride (from the library instrumentation
    // jar being on the classpath), don't use it as the delegate - it is backed by OTel context
    // just like us, so it cannot serve as a baseline for context propagation checks. Use a simple
    // ThreadLocal-based storage instead to independently track which gRPC contexts have been
    // explicitly attached on each thread.
    if (storage.getClass().getName().equals("io.grpc.override.ContextStorageOverride")) {
      STORAGE_REFERENCE.compareAndSet(
          null, new ContextStorageBridge(new ThreadLocalContextStorage()));
    } else {
      STORAGE_REFERENCE.compareAndSet(null, new ContextStorageBridge(storage));
    }
    return getStorage();
  }

  /**
   * Simple ThreadLocal-based Context.Storage that tracks gRPC context attachment independently of
   * the OTel context bridge. Used as the originalStorage baseline when the real original storage is
   * itself an OTel bridge (ContextStorageOverride).
   */
  private static class ThreadLocalContextStorage extends Context.Storage {
    private static final ThreadLocal<Context> current = new ThreadLocal<>();

    @Override
    public Context doAttach(Context toAttach) {
      Context previous = current.get();
      current.set(toAttach);
      return previous != null ? previous : Context.ROOT;
    }

    @Override
    public void detach(Context toDetach, Context toRestore) {
      current.set(toRestore);
    }

    @Override
    public Context current() {
      return current.get();
    }
  }

  private GrpcSingletons() {}
}
