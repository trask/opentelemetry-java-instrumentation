/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.apachedubbo.v2_7;

import com.google.auto.value.AutoValue;
import java.net.InetSocketAddress;
import javax.annotation.Nullable;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcInvocation;

@AutoValue
public abstract class DubboRequest {

  static DubboRequest create(RpcInvocation invocation, RpcContext context) {
    // In dubbo 3 RpcContext delegates to a ThreadLocal context. We copy the url and remote address
    // here to ensure we can access them from the thread that ends the span.
    return new AutoValue_DubboRequest(
        invocation,
        context,
        context.getUrl(),
        context.getRemoteAddress(),
        context.getLocalAddress());
  }

  abstract RpcInvocation invocation();

  abstract RpcContext contextValue();

  abstract URL urlValue();

  @Nullable
  abstract InetSocketAddress remoteAddressValue();

  @Nullable
  abstract InetSocketAddress localAddressValue();

  public RpcContext getContext() {
    return contextValue();
  }

  /**
   * @deprecated Use {@link #getContext()} instead. Will be removed in a future release.
   */
  @Deprecated // will be removed in a future release
  public RpcContext context() {
    return getContext();
  }

  public URL getUrl() {
    return urlValue();
  }

  /**
   * @deprecated Use {@link #getUrl()} instead. Will be removed in a future release.
   */
  @Deprecated // will be removed in a future release
  public URL url() {
    return getUrl();
  }

  @Nullable
  public InetSocketAddress getRemoteAddress() {
    return remoteAddressValue();
  }

  /**
   * @deprecated Use {@link #getRemoteAddress()} instead. Will be removed in a future release.
   */
  @Deprecated // will be removed in a future release
  @Nullable
  public InetSocketAddress remoteAddress() {
    return getRemoteAddress();
  }

  @Nullable
  public InetSocketAddress getLocalAddress() {
    return localAddressValue();
  }

  /**
   * @deprecated Use {@link #getLocalAddress()} instead. Will be removed in a future release.
   */
  @Deprecated // will be removed in a future release
  @Nullable
  public InetSocketAddress localAddress() {
    return getLocalAddress();
  }
}
