
### HTTP Client Instrumentations

1. **Apache HttpClient 4.3** (`apache-httpclient-4.3/library`)
   - **Response Type:** `org.apache.http.HttpResponse` (framework class)
   - **Request Type:** `io.opentelemetry.instrumentation.apachehttpclient.v4_3.ApacheHttpClientRequest` (wrapper)

2. **Apache HttpClient 5.2** (`apache-httpclient-5.2/library`)
   - **Response Type:** `org.apache.hc.core5.http.HttpResponse` (framework class)
   - **Request Type:** `io.opentelemetry.instrumentation.apachehttpclient.v5_2.ApacheHttpClientRequest` (wrapper)

3. **Armeria Client 1.3** (`armeria-1.3/library`)
   - **Request Type:** `com.linecorp.armeria.client.ClientRequestContext` (framework class)
   - **Response Type:** `com.linecorp.armeria.common.logging.RequestLog` (framework class)

4. **Java HttpClient** (`java-http-client/library`)
   - **Request Type:** `java.net.http.HttpRequest` (JDK class)
   - **Response Type:** `java.net.http.HttpResponse<?>` (JDK class)

5. **Jetty HttpClient 9.2** (`jetty-httpclient-9.2/library`)
   - **Request Type:** `org.eclipse.jetty.client.api.Request` (framework class)
   - **Response Type:** `org.eclipse.jetty.client.api.Response` (framework class)

6. **Netty Client 4.1** (`netty-4.1/library`)
   - **Request Type:** `io.opentelemetry.instrumentation.netty.common.v4_0.HttpRequestAndChannel` (wrapper)
   - **Response Type:** `io.netty.handler.codec.http.HttpResponse` (framework class)

7. **OkHttp 3.0** (`okhttp-3.0/library`)
   - **Request Type:** `okhttp3.Interceptor.Chain` (framework class)
   - **Response Type:** `okhttp3.Response` (framework class)

8. **Spring Web 3.1** (`spring-web-3.1/library`)
   - **Request Type:** `org.springframework.http.HttpRequest` (framework class)
   - **Response Type:** `org.springframework.http.client.ClientHttpResponse` (framework class)

9. **Spring WebFlux Client 5.3** (`spring-webflux-5.3/library`)
   - **Request Type:** `org.springframework.web.reactive.function.client.ClientRequest` (framework class)
   - **Response Type:** `org.springframework.web.reactive.function.client.ClientResponse` (framework class)

### HTTP Server Instrumentations

1. **Armeria Server 1.3** (`armeria-1.3/library`)
   - **Request Type:** `com.linecorp.armeria.server.ServiceRequestContext` (framework class)
   - **Response Type:** `com.linecorp.armeria.common.logging.RequestLog` (framework class)

2. **Netty Server 4.1** (`netty-4.1/library`)
   - **Request Type:** `io.opentelemetry.instrumentation.netty.common.v4_0.HttpRequestAndChannel` (wrapper)
   - **Response Type:** `io.netty.handler.codec.http.HttpResponse` (framework class)

3. **Restlet 1.1 & 2.0** (`restlet-1.1/library`, `restlet-2.0/library`)
   - **Request Type:** `org.restlet.Request` (framework class)
   - **Response Type:** `org.restlet.Response` (framework class)

4. **Spring WebFlux Server 5.3** (`spring-webflux-5.3/library`)
   - **Both Types:** `org.springframework.web.server.ServerWebExchange` (framework class)

5. **Spring WebMVC 5.3** (`spring-webmvc-5.3/library`)
   - **Request Type:** `javax.servlet.http.HttpServletRequest` (framework class)
   - **Response Type:** `javax.servlet.http.HttpServletResponse` (framework class)

6. **Spring WebMVC 6.0** (`spring-webmvc-6.0/library`)
   - **Request Type:** `javax.servlet.http.HttpServletRequest` (framework class)
   - **Response Type:** `javax.servlet.http.HttpServletResponse` (framework class)
