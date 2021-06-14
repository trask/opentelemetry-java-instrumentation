/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.cxf;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesNoArguments;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.Server;

public class JaxWsServerFactoryBeanInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    // important to instrument this underlying class, instead of org.apache.cxf.jaxws.EndpointImpl,
    // because tomee/openejb has it's own copy/variant of org.apache.cxf.jaxws.EndpointImpl
    // (org.apache.openejb.server.cxf.CxfEndpoint)
    return named("org.apache.cxf.jaxws.JaxWsServerFactoryBean");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        named("create")
            .and(takesNoArguments().and(returns(named("org.apache.cxf.endpoint.Server")))),
        JaxWsServerFactoryBeanInstrumentation.class.getName() + "$CreateAdvice");
  }

  @SuppressWarnings("unused")
  public static class CreateAdvice {

    @Advice.OnMethodExit(suppress = Throwable.class)
    public static void onEnter(@Advice.Return Server server) {
      Endpoint endpoint = server.getEndpoint();
      endpoint.getInInterceptors().add(new TracingStartInInterceptor());
      endpoint.getInInterceptors().add(new TracingEndInInterceptor());
      endpoint.getOutFaultInterceptors().add(new TracingOutFaultInterceptor());
    }
  }
}
