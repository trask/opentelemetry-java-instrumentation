/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.couchbase.v2_0;

import io.opentelemetry.instrumentation.api.incubator.semconv.db.DbClientAttributesGetter;
import io.opentelemetry.instrumentation.api.internal.SemconvStability;
import io.opentelemetry.semconv.incubating.DbIncubatingAttributes;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import javax.annotation.Nullable;

final class CouchbaseAttributesGetter
    implements DbClientAttributesGetter<CouchbaseRequestInfo, Void> {

  @SuppressWarnings("deprecation") // using deprecated DbSystemIncubatingValues
  @Override
  public String getDbSystemName(CouchbaseRequestInfo couchbaseRequest) {
    return DbIncubatingAttributes.DbSystemIncubatingValues.COUCHBASE;
  }

  @Override
  @Nullable
  public String getDbNamespace(CouchbaseRequestInfo couchbaseRequest) {
    return couchbaseRequest.bucket();
  }

  @Override
  @Nullable
  public String getDbQueryText(CouchbaseRequestInfo couchbaseRequest) {
    return couchbaseRequest.statement();
  }

  @Override
  @Nullable
  public String getDbOperationName(CouchbaseRequestInfo couchbaseRequest) {
    // Under stable semconv, DB_OPERATION_NAME should not be extracted from query text
    // For N1QL queries (isMethodCall() == false), the operation is extracted from query text
    if (SemconvStability.emitStableDatabaseSemconv() && !couchbaseRequest.isMethodCall()) {
      return null;
    }
    return couchbaseRequest.operation();
  }

  @Override
  public InetSocketAddress getNetworkPeerInetSocketAddress(
      CouchbaseRequestInfo request, @Nullable Void unused) {
    SocketAddress address = request.getPeerAddress();
    if (address instanceof InetSocketAddress) {
      return (InetSocketAddress) address;
    }
    return null;
  }
}
