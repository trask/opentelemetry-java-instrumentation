
For all of the "unchecked" or "rawtypes" @SuppressWarnings in the files below:

Move it from a method level annotation to a statement level annotation on the specific statement(s) where it's needed.


---

# "unchecked" and "rawtypes" @SuppressWarnings to review

## declarative-config-bridge

- [ ] declarative-config-bridge/src/main/java/io/opentelemetry/instrumentation/config/bridge/DeclarativeConfigPropertiesBridge.java:111

## instrumentation/apache-dubbo-2.7/library-autoconfigure

- [ ] instrumentation/apache-dubbo-2.7/library-autoconfigure/src/main/java/io/opentelemetry/instrumentation/apachedubbo/v2_7/DubboHeadersGetter.java:34
- [ ] instrumentation/apache-dubbo-2.7/library-autoconfigure/src/main/java/io/opentelemetry/instrumentation/apachedubbo/v2_7/DubboTelemetryBuilder.java:74
- [ ] instrumentation/apache-dubbo-2.7/library-autoconfigure/src/main/java/io/opentelemetry/instrumentation/apachedubbo/v2_7/DubboTelemetryBuilder.java:98

## instrumentation/apache-dubbo-2.7/testing

- [ ] instrumentation/apache-dubbo-2.7/testing/src/main/java/io/opentelemetry/instrumentation/apachedubbo/v2_7/AbstractDubboTest.java:90
- [ ] instrumentation/apache-dubbo-2.7/testing/src/main/java/io/opentelemetry/instrumentation/apachedubbo/v2_7/AbstractDubboTraceChainTest.java:114

## instrumentation/aws-lambda/aws-lambda-events-2.2/library

- [ ] instrumentation/aws-lambda/aws-lambda-events-2.2/library/src/main/java/io/opentelemetry/instrumentation/awslambdaevents/v2_2/TracingRequestWrapper.java:77
- [ ] instrumentation/aws-lambda/aws-lambda-events-2.2/library/src/main/java/io/opentelemetry/instrumentation/awslambdaevents/v2_2/TracingRequestWrapperBase.java:59

## instrumentation/aws-lambda/aws-lambda-events-3.11/library

- [ ] instrumentation/aws-lambda/aws-lambda-events-3.11/library/src/main/java/io/opentelemetry/instrumentation/awslambdaevents/v3_11/TracingRequestWrapper.java:73
- [ ] instrumentation/aws-lambda/aws-lambda-events-3.11/library/src/main/java/io/opentelemetry/instrumentation/awslambdaevents/v3_11/TracingRequestWrapperBase.java:59

## instrumentation/aws-lambda/aws-lambda-events-common-2.2/library

- [ ] instrumentation/aws-lambda/aws-lambda-events-common-2.2/library/src/main/java/io/opentelemetry/instrumentation/awslambdaevents/common/v2_2/internal/SerializationUtil.java:49
- [ ] instrumentation/aws-lambda/aws-lambda-events-common-2.2/library/src/main/java/io/opentelemetry/instrumentation/awslambdaevents/common/v2_2/internal/SerializationUtil.java:64
- [ ] instrumentation/aws-lambda/aws-lambda-events-common-2.2/library/src/main/java/io/opentelemetry/instrumentation/awslambdaevents/common/v2_2/internal/SerializationUtil.java:72

## instrumentation/aws-sdk/aws-sdk-1.11/testing

- [ ] instrumentation/aws-sdk/aws-sdk-1.11/testing/src/main/java/io/opentelemetry/instrumentation/awssdk/v1_11/AbstractBaseAwsClientTest.java:123

## instrumentation/aws-sdk/aws-sdk-2.2/library

- [ ] instrumentation/aws-sdk/aws-sdk-2.2/library/src/main/java/io/opentelemetry/instrumentation/awssdk/v2_2/internal/AwsJsonProtocolFactoryAccess.java:43
- [ ] instrumentation/aws-sdk/aws-sdk-2.2/library/src/main/java/io/opentelemetry/instrumentation/awssdk/v2_2/internal/AwsJsonProtocolFactoryAccess.java:46
- [ ] instrumentation/aws-sdk/aws-sdk-2.2/library/src/main/java/io/opentelemetry/instrumentation/awssdk/v2_2/internal/AwsJsonProtocolFactoryAccess.java:72
- [ ] instrumentation/aws-sdk/aws-sdk-2.2/library/src/main/java/io/opentelemetry/instrumentation/awssdk/v2_2/internal/FieldMapping.java:79
- [ ] instrumentation/aws-sdk/aws-sdk-2.2/library/src/main/java/io/opentelemetry/instrumentation/awssdk/v2_2/internal/SqsImpl.java:311

## instrumentation/aws-sdk/aws-sdk-2.2/testing

- [ ] instrumentation/aws-sdk/aws-sdk-2.2/testing/src/main/java/io/opentelemetry/instrumentation/awssdk/v2_2/AbstractAws2ClientCoreTest.java:305
- [ ] instrumentation/aws-sdk/aws-sdk-2.2/testing/src/main/java/io/opentelemetry/instrumentation/awssdk/v2_2/AbstractAws2SqsBaseTest.java:83

## instrumentation/elasticsearch/elasticsearch-rest-7.0/library

- [ ] instrumentation/elasticsearch/elasticsearch-rest-7.0/library/src/main/java/io/opentelemetry/instrumentation/elasticsearch/rest/v7_0/RestClientWrapper.java:133
- [ ] instrumentation/elasticsearch/elasticsearch-rest-7.0/library/src/main/java/io/opentelemetry/instrumentation/elasticsearch/rest/v7_0/RestClientWrapper.java:174

## instrumentation/graphql-java/graphql-java-common/library

- [ ] instrumentation/graphql-java/graphql-java-common/library/src/main/java/io/opentelemetry/instrumentation/graphql/internal/OpenTelemetryInstrumentationHelper.java:161

## instrumentation/grpc-1.6/library

- [ ] instrumentation/grpc-1.6/library/src/main/java/io/opentelemetry/instrumentation/grpc/v1_6/GrpcTelemetryBuilder.java:105
- [ ] instrumentation/grpc-1.6/library/src/main/java/io/opentelemetry/instrumentation/grpc/v1_6/GrpcTelemetryBuilder.java:129
- [ ] instrumentation/grpc-1.6/library/src/main/java/io/opentelemetry/instrumentation/grpc/v1_6/TracingClientInterceptor.java:40
- [ ] instrumentation/grpc-1.6/library/src/main/java/io/opentelemetry/instrumentation/grpc/v1_6/TracingClientInterceptor.java:44
- [ ] instrumentation/grpc-1.6/library/src/main/java/io/opentelemetry/instrumentation/grpc/v1_6/TracingServerInterceptor.java:40
- [ ] instrumentation/grpc-1.6/library/src/main/java/io/opentelemetry/instrumentation/grpc/v1_6/TracingServerInterceptor.java:44

## instrumentation/grpc-1.6/testing

- [ ] instrumentation/grpc-1.6/testing/src/main/java/io/opentelemetry/instrumentation/grpc/v1_6/AbstractGrpcStreamingTest.java:427

## instrumentation/influxdb-2.4/javaagent

- [ ] instrumentation/influxdb-2.4/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/influxdb/v2_4/InfluxDbObjetWrapper.java:15

## instrumentation/jedis/jedis-common/javaagent

- [ ] instrumentation/jedis/jedis-common/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/jedis/JedisRequestContext.java:56

## instrumentation/jms/jms-1.1/javaagent

- [ ] instrumentation/jms/jms-1.1/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/jms/v1_1/JavaxMessageAdapter.java:40

## instrumentation/jms/jms-3.0/javaagent

- [ ] instrumentation/jms/jms-3.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/jms/v3_0/JakartaMessageAdapter.java:40

## instrumentation/jmx-metrics/library

- [ ] instrumentation/jmx-metrics/library/src/main/java/io/opentelemetry/instrumentation/jmx/yaml/RuleParser.java:128
- [ ] instrumentation/jmx-metrics/library/src/main/java/io/opentelemetry/instrumentation/jmx/yaml/RuleParser.java:49
- [ ] instrumentation/jmx-metrics/library/src/main/java/io/opentelemetry/instrumentation/jmx/yaml/RuleParser.java:72
- [ ] instrumentation/jmx-metrics/library/src/main/java/io/opentelemetry/instrumentation/jmx/yaml/RuleParser.java:95

## instrumentation/kafka/kafka-clients/kafka-clients-0.11/javaagent

- [ ] instrumentation/kafka/kafka-clients/kafka-clients-0.11/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/kafkaclients/v0_11/metrics/KafkaMetricsUtil.java:19

## instrumentation/kafka/kafka-clients/kafka-clients-0.11/testing

- [ ] instrumentation/kafka/kafka-clients/kafka-clients-0.11/testing/src/main/java/io/opentelemetry/instrumentation/kafkaclients/common/v0_11/internal/AbstractOpenTelemetryMetricsReporterTest.java:150
- [ ] instrumentation/kafka/kafka-clients/kafka-clients-0.11/testing/src/main/java/io/opentelemetry/instrumentation/kafkaclients/common/v0_11/internal/KafkaTestUtil.java:24

## instrumentation/kafka/kafka-clients/kafka-clients-2.6/library

- [ ] instrumentation/kafka/kafka-clients/kafka-clients-2.6/library/src/main/java/io/opentelemetry/instrumentation/kafkaclients/v2_6/internal/OpenTelemetryConsumerInterceptor.java:74
- [ ] instrumentation/kafka/kafka-clients/kafka-clients-2.6/library/src/main/java/io/opentelemetry/instrumentation/kafkaclients/v2_6/internal/OpenTelemetryProducerInterceptor.java:58
- [ ] instrumentation/kafka/kafka-clients/kafka-clients-2.6/library/src/main/java/io/opentelemetry/instrumentation/kafkaclients/v2_6/KafkaTelemetry.java:119
- [ ] instrumentation/kafka/kafka-clients/kafka-clients-2.6/library/src/main/java/io/opentelemetry/instrumentation/kafkaclients/v2_6/KafkaTelemetry.java:89

## instrumentation/kafka/kafka-clients/kafka-clients-common-0.11/library

- [ ] instrumentation/kafka/kafka-clients/kafka-clients-common-0.11/library/src/main/java/io/opentelemetry/instrumentation/kafkaclients/common/v0_11/internal/OpenTelemetryMetricsReporter.java:175

## instrumentation/lettuce/lettuce-5.1/library

- [ ] instrumentation/lettuce/lettuce-5.1/library/src/main/java/io/lettuce/core/protocol/OtelCommandArgsUtil.java:35

## instrumentation/lettuce/lettuce-5.1/testing

- [ ] instrumentation/lettuce/lettuce-5.1/testing/src/main/java/io/opentelemetry/instrumentation/lettuce/v5_1/AbstractLettuceSyncClientTest.java:160

## instrumentation/nats/nats-2.17/javaagent

- [ ] instrumentation/nats/nats-2.17/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/nats/v2_17/ConnectionRequestInstrumentation.java:431

## instrumentation/nats/nats-2.17/library

- [ ] instrumentation/nats/nats-2.17/library/src/main/java/io/opentelemetry/instrumentation/nats/v2_17/OpenTelemetryConnection.java:237

## instrumentation/netty/netty-3.8/javaagent

- [ ] instrumentation/netty/netty-3.8/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/netty/v3_8/util/HttpSchemeUtil.java:15

## instrumentation/netty/netty-4.0/javaagent

- [ ] instrumentation/netty/netty-4.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/netty/v4_0/AttributeKeys.java:42

## instrumentation/netty/netty-common-4.0/javaagent

- [ ] instrumentation/netty/netty-common-4.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/netty/v4/common/FutureListenerWrappers.java:49
- [ ] instrumentation/netty/netty-common-4.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/netty/v4/common/NettyFutureInstrumentation.java:128
- [ ] instrumentation/netty/netty-common-4.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/netty/v4/common/NettyFutureInstrumentation.java:91

## instrumentation/netty/netty-common-4.0/library

- [ ] instrumentation/netty/netty-common-4.0/library/src/main/java/io/opentelemetry/instrumentation/netty/common/v4_0/internal/HttpSchemeUtil.java:19

## instrumentation/openai/openai-java-1.1/library

- [ ] instrumentation/openai/openai-java-1.1/library/src/main/java/io/opentelemetry/instrumentation/openai/v1_1/ChatCompletionEventsHelper.java:412
- [ ] instrumentation/openai/openai-java-1.1/library/src/main/java/io/opentelemetry/instrumentation/openai/v1_1/DelegatingInvocationHandler.java:36

## instrumentation/opentelemetry-api/opentelemetry-api-1.0/javaagent

- [ ] instrumentation/opentelemetry-api/opentelemetry-api-1.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/opentelemetryapi/context/ContextKeyBridge.java:106
- [ ] instrumentation/opentelemetry-api/opentelemetry-api-1.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/opentelemetryapi/context/ContextKeyBridge.java:64
- [ ] instrumentation/opentelemetry-api/opentelemetry-api-1.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/opentelemetryapi/context/ContextKeyBridge.java:96
- [ ] instrumentation/opentelemetry-api/opentelemetry-api-1.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/opentelemetryapi/trace/ApplicationSpan.java:60
- [ ] instrumentation/opentelemetry-api/opentelemetry-api-1.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/opentelemetryapi/trace/ApplicationSpanBuilder.java:89
- [ ] instrumentation/opentelemetry-api/opentelemetry-api-1.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/opentelemetryapi/trace/Bridging.java:108
- [ ] instrumentation/opentelemetry-api/opentelemetry-api-1.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/opentelemetryapi/trace/Bridging.java:124

## instrumentation/opentelemetry-api/opentelemetry-api-1.15/javaagent

- [ ] instrumentation/opentelemetry-api/opentelemetry-api-1.15/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/opentelemetryapi/v1_15/metrics/ApplicationMeter115.java:32

## instrumentation/opentelemetry-api/opentelemetry-api-1.27/javaagent

- [ ] instrumentation/opentelemetry-api/opentelemetry-api-1.27/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/opentelemetryapi/v1_27/logs/ApplicationLogRecordBuilder.java:84

## instrumentation/opentelemetry-api/opentelemetry-api-1.42/javaagent

- [ ] instrumentation/opentelemetry-api/opentelemetry-api-1.42/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/opentelemetryapi/v1_42/logs/ApplicationLogRecordBuilder142.java:33

## instrumentation/opentelemetry-api/opentelemetry-api-1.50/javaagent

- [ ] instrumentation/opentelemetry-api/opentelemetry-api-1.50/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/opentelemetryapi/v1_50/incubator/logs/ApplicationLogRecordBuilder150Incubator.java:120
- [ ] instrumentation/opentelemetry-api/opentelemetry-api-1.50/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/opentelemetryapi/v1_50/incubator/logs/ApplicationLogRecordBuilder150Incubator.java:139
- [ ] instrumentation/opentelemetry-api/opentelemetry-api-1.50/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/opentelemetryapi/v1_50/incubator/logs/ApplicationLogRecordBuilder150Incubator.java:158

## instrumentation/play/play-mvc/play-mvc-2.6/javaagent

- [ ] instrumentation/play/play-mvc/play-mvc-2.6/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/play/v2_6/Play26Singletons.java:73

## instrumentation/quartz-2.0/library

- [ ] instrumentation/quartz-2.0/library/src/main/java/io/opentelemetry/instrumentation/quartz/v2_0/QuartzTelemetry.java:58

## instrumentation/r2dbc-1.0/library

- [ ] instrumentation/r2dbc-1.0/library/src/main/java/io/opentelemetry/instrumentation/r2dbc/v1_0/internal/R2dbcSqlCommenterUtil.java:41
- [ ] instrumentation/r2dbc-1.0/library/src/main/java/io/opentelemetry/instrumentation/r2dbc/v1_0/R2dbcTelemetryBuilder.java:61

## instrumentation/reactor/reactor-3.1/library

- [ ] instrumentation/reactor/reactor-3.1/library/src/main/java/io/opentelemetry/instrumentation/reactor/v3_1/ContextPropagationOperator.java:208
- [ ] instrumentation/reactor/reactor-3.1/library/src/main/java/io/opentelemetry/instrumentation/reactor/v3_1/ContextPropagationOperator.java:229
- [ ] instrumentation/reactor/reactor-3.1/library/src/main/java/io/opentelemetry/instrumentation/reactor/v3_1/ContextPropagationOperator.java:249
- [ ] instrumentation/reactor/reactor-3.1/library/src/main/java/io/opentelemetry/instrumentation/reactor/v3_1/ContextPropagationOperator.java:323
- [ ] instrumentation/reactor/reactor-3.1/library/src/main/java/io/opentelemetry/instrumentation/reactor/v3_1/ContextPropagationOperator.java:352

## instrumentation/reactor/reactor-kafka-1.0/javaagent

- [ ] instrumentation/reactor/reactor-kafka-1.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/reactor/kafka/v1_0/InstrumentedKafkaFlux.java:111
- [ ] instrumentation/reactor/reactor-kafka-1.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/reactor/kafka/v1_0/InstrumentedKafkaFlux.java:31
- [ ] instrumentation/reactor/reactor-kafka-1.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/reactor/kafka/v1_0/TracingDisablingKafkaFlux.java:81

## instrumentation/reactor/reactor-kafka-1.0/testing

- [ ] instrumentation/reactor/reactor-kafka-1.0/testing/src/main/java/io/opentelemetry/javaagent/instrumentation/reactor/kafka/v1_0/AbstractReactorKafkaTest.java:100
- [ ] instrumentation/reactor/reactor-kafka-1.0/testing/src/main/java/io/opentelemetry/javaagent/instrumentation/reactor/kafka/v1_0/AbstractReactorKafkaTest.java:118

## instrumentation/resources/library

- [ ] instrumentation/resources/library/src/main/java/io/opentelemetry/instrumentation/resources/AttributeResourceProvider.java:40
- [ ] instrumentation/resources/library/src/main/java/io/opentelemetry/instrumentation/resources/internal/ContainerResourceComponentProvider.java:18
- [ ] instrumentation/resources/library/src/main/java/io/opentelemetry/instrumentation/resources/internal/HostResourceComponentProvider.java:20
- [ ] instrumentation/resources/library/src/main/java/io/opentelemetry/instrumentation/resources/internal/JarResourceComponentProvider.java:17
- [ ] instrumentation/resources/library/src/main/java/io/opentelemetry/instrumentation/resources/internal/ManifestResourceComponentProvider.java:17
- [ ] instrumentation/resources/library/src/main/java/io/opentelemetry/instrumentation/resources/internal/ProcessResourceComponentProvider.java:19

## instrumentation/restlet/restlet-2.0/library

- [ ] instrumentation/restlet/restlet-2.0/library/src/main/java/io/opentelemetry/instrumentation/restlet/v2_0/internal/MessageAttributesAccessor.java:96
- [ ] instrumentation/restlet/restlet-2.0/library/src/main/java/io/opentelemetry/instrumentation/restlet/v2_0/internal/RestletHeadersGetter.java:69

## instrumentation/restlet/restlet-2.0/testing

- [ ] instrumentation/restlet/restlet-2.0/testing/src/main/java/io/opentelemetry/instrumentation/restlet/v2_0/RestletAppTestBase.java:94

## instrumentation/rmi/javaagent

- [ ] instrumentation/rmi/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/rmi/context/ContextPayload.java:49

## instrumentation/rocketmq/rocketmq-client/rocketmq-client-5.0/javaagent

- [ ] instrumentation/rocketmq/rocketmq-client/rocketmq-client-5.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/rocketmqclient/v5_0/ReceiveSpanFinishingCallback.java:30

## instrumentation/rocketmq/rocketmq-client/rocketmq-client-5.0/testing

- [ ] instrumentation/rocketmq/rocketmq-client/rocketmq-client-5.0/testing/src/main/java/io/opentelemetry/instrumentation/rocketmqclient/v5_0/RocketMqProxyContainer.java:23

## instrumentation/rxjava/rxjava-2.0/library

- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/RxJava2AsyncOperationEndStrategy.java:103
- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/RxJava2AsyncOperationEndStrategy.java:95
- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/TracingAssembly.java:170
- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/TracingAssembly.java:194
- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/TracingAssembly.java:214
- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/TracingAssembly.java:250
- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/TracingAssembly.java:265
- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/TracingAssembly.java:329
- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/TracingAssembly.java:59
- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/TracingAssembly.java:65
- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/TracingAssembly.java:72
- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/TracingAssembly.java:78
- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/TracingAssembly.java:84
- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/TracingAssembly.java:90
- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/TracingObserver.java:83
- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/TracingParallelFlowable.java:41
- [ ] instrumentation/rxjava/rxjava-2.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/TracingParallelFlowable.java:48

## instrumentation/rxjava/rxjava-2.0/testing

- [ ] instrumentation/rxjava/rxjava-2.0/testing/src/main/java/io/opentelemetry/instrumentation/rxjava/v2_0/AbstractRxJava2Test.java:866

## instrumentation/rxjava/rxjava-3.0/library

- [ ] instrumentation/rxjava/rxjava-3.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_0/TracingAssembly.java:174
- [ ] instrumentation/rxjava/rxjava-3.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_0/TracingAssembly.java:198
- [ ] instrumentation/rxjava/rxjava-3.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_0/TracingAssembly.java:218
- [ ] instrumentation/rxjava/rxjava-3.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_0/TracingAssembly.java:252
- [ ] instrumentation/rxjava/rxjava-3.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_0/TracingAssembly.java:267
- [ ] instrumentation/rxjava/rxjava-3.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_0/TracingAssembly.java:331
- [ ] instrumentation/rxjava/rxjava-3.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_0/TracingAssembly.java:63
- [ ] instrumentation/rxjava/rxjava-3.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_0/TracingAssembly.java:69
- [ ] instrumentation/rxjava/rxjava-3.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_0/TracingAssembly.java:76
- [ ] instrumentation/rxjava/rxjava-3.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_0/TracingAssembly.java:82
- [ ] instrumentation/rxjava/rxjava-3.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_0/TracingAssembly.java:88
- [ ] instrumentation/rxjava/rxjava-3.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_0/TracingAssembly.java:94
- [ ] instrumentation/rxjava/rxjava-3.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_0/TracingParallelFlowable.java:41
- [ ] instrumentation/rxjava/rxjava-3.0/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_0/TracingParallelFlowable.java:48

## instrumentation/rxjava/rxjava-3.1.1/library

- [ ] instrumentation/rxjava/rxjava-3.1.1/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_1_1/TracingAssembly.java:174
- [ ] instrumentation/rxjava/rxjava-3.1.1/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_1_1/TracingAssembly.java:217
- [ ] instrumentation/rxjava/rxjava-3.1.1/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_1_1/TracingAssembly.java:237
- [ ] instrumentation/rxjava/rxjava-3.1.1/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_1_1/TracingAssembly.java:252
- [ ] instrumentation/rxjava/rxjava-3.1.1/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_1_1/TracingAssembly.java:267
- [ ] instrumentation/rxjava/rxjava-3.1.1/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_1_1/TracingAssembly.java:331
- [ ] instrumentation/rxjava/rxjava-3.1.1/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_1_1/TracingAssembly.java:63
- [ ] instrumentation/rxjava/rxjava-3.1.1/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_1_1/TracingAssembly.java:69
- [ ] instrumentation/rxjava/rxjava-3.1.1/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_1_1/TracingAssembly.java:76
- [ ] instrumentation/rxjava/rxjava-3.1.1/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_1_1/TracingAssembly.java:82
- [ ] instrumentation/rxjava/rxjava-3.1.1/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_1_1/TracingAssembly.java:88
- [ ] instrumentation/rxjava/rxjava-3.1.1/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_1_1/TracingAssembly.java:94
- [ ] instrumentation/rxjava/rxjava-3.1.1/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_1_1/TracingParallelFlowable.java:41
- [ ] instrumentation/rxjava/rxjava-3.1.1/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3_1_1/TracingParallelFlowable.java:48

## instrumentation/rxjava/rxjava-3-common/library

- [ ] instrumentation/rxjava/rxjava-3-common/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3/common/RxJava3AsyncOperationEndStrategy.java:103
- [ ] instrumentation/rxjava/rxjava-3-common/library/src/main/java/io/opentelemetry/instrumentation/rxjava/v3/common/RxJava3AsyncOperationEndStrategy.java:95

## instrumentation/rxjava/rxjava-3-common/testing

- [ ] instrumentation/rxjava/rxjava-3-common/testing/src/main/java/io/opentelemetry/instrumentation/rxjava/v3/common/AbstractRxJava3Test.java:867

## instrumentation/servlet/servlet-common/javaagent

- [ ] instrumentation/servlet/servlet-common/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/servlet/ServletHelper.java:88

## instrumentation/servlet/servlet-javax-common/javaagent

- [ ] instrumentation/servlet/servlet-javax-common/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/servlet/javax/JavaxServletAccessor.java:70
- [ ] instrumentation/servlet/servlet-javax-common/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/servlet/javax/JavaxServletAccessor.java:77

## instrumentation/spring/spring-boot-autoconfigure

- [ ] instrumentation/spring/spring-boot-autoconfigure/src/main/java/io/opentelemetry/instrumentation/spring/autoconfigure/internal/instrumentation/kafka/ConcurrentKafkaListenerContainerFactoryPostProcessor.java:26
- [ ] instrumentation/spring/spring-boot-autoconfigure/src/main/java/io/opentelemetry/instrumentation/spring/autoconfigure/internal/properties/SpringConfigProperties.java:196
- [ ] instrumentation/spring/spring-boot-autoconfigure/src/main/java/io/opentelemetry/instrumentation/spring/autoconfigure/internal/properties/SpringConfigProperties.java:227

## instrumentation/spring/spring-boot-resources/javaagent

- [ ] instrumentation/spring/spring-boot-resources/javaagent/src/main/java/io/opentelemetry/instrumentation/spring/resources/SpringBootServiceNameDetector.java:221
- [ ] instrumentation/spring/spring-boot-resources/javaagent/src/main/java/io/opentelemetry/instrumentation/spring/resources/SystemHelper.java:58

## instrumentation/spring/spring-integration-4.1/library

- [ ] instrumentation/spring/spring-integration-4.1/library/src/main/java/io/opentelemetry/instrumentation/spring/integration/v4_1/MessageHeadersGetter.java:26
- [ ] instrumentation/spring/spring-integration-4.1/library/src/main/java/io/opentelemetry/instrumentation/spring/integration/v4_1/MessageHeadersGetter.java:55
- [ ] instrumentation/spring/spring-integration-4.1/library/src/main/java/io/opentelemetry/instrumentation/spring/integration/v4_1/MessageHeadersSetter.java:32
- [ ] instrumentation/spring/spring-integration-4.1/library/src/main/java/io/opentelemetry/instrumentation/spring/integration/v4_1/TracingChannelInterceptor.java:191
- [ ] instrumentation/spring/spring-integration-4.1/library/src/main/java/io/opentelemetry/instrumentation/spring/integration/v4_1/TracingChannelInterceptor.java:256

## instrumentation/spring/spring-kafka-2.7/testing

- [ ] instrumentation/spring/spring-kafka-2.7/testing/src/main/java/io/opentelemetry/testing/AbstractSpringKafkaTest.java:68

## instrumentation/spring/spring-pulsar-1.0/testing

- [ ] instrumentation/spring/spring-pulsar-1.0/testing/src/main/java/io/opentelemetry/instrumentation/spring/pulsar/v1_0/AbstractSpringPulsarTest.java:69

## instrumentation/undertow-1.4/javaagent

- [ ] instrumentation/undertow-1.4/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/undertow/UndertowHelper.java:70
- [ ] instrumentation/undertow-1.4/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/undertow/UndertowHelper.java:81

## instrumentation/zio/zio-2.0/javaagent

- [ ] instrumentation/zio/zio-2.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/zio/v2_0/TracingSupervisor.java:18
- [ ] instrumentation/zio/zio-2.0/javaagent/src/main/java/io/opentelemetry/javaagent/instrumentation/zio/v2_0/TracingSupervisor.java:28

## instrumentation-annotations-support

- [ ] instrumentation-annotations-support/src/main/java/io/opentelemetry/instrumentation/api/annotation/support/AnnotationReflectionHelper.java:89
- [ ] instrumentation-annotations-support/src/main/java/io/opentelemetry/instrumentation/api/annotation/support/async/AsyncOperationEndSupport.java:65
- [ ] instrumentation-annotations-support/src/main/java/io/opentelemetry/instrumentation/api/annotation/support/AttributeBindingFactory.java:117
- [ ] instrumentation-annotations-support/src/main/java/io/opentelemetry/instrumentation/api/annotation/support/AttributeBindingFactory.java:313

## instrumentation-api

- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/instrumenter/Instrumenter.java:88
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/instrumenter/SpanStatusExtractor.java:30
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/instrumenter/UnsafeAttributes.java:31
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/internal/cache/concurrentlinkedhashmap/ConcurrentLinkedHashMap.java:1493
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/internal/cache/concurrentlinkedhashmap/ConcurrentLinkedHashMap.java:214
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/internal/cache/concurrentlinkedhashmap/LinkedDeque.java:318
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/internal/cache/concurrentlinkedhashmap/Weighers.java:111
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/internal/cache/concurrentlinkedhashmap/Weighers.java:128
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/internal/cache/concurrentlinkedhashmap/Weighers.java:145
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/internal/cache/concurrentlinkedhashmap/Weighers.java:162
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/internal/cache/concurrentlinkedhashmap/Weighers.java:179
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/internal/cache/concurrentlinkedhashmap/Weighers.java:64
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/internal/cache/concurrentlinkedhashmap/Weighers.java:75
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/internal/cache/weaklockfree/WeakConcurrentMap.java:120
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/internal/Experimental.java:58
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/internal/Experimental.java:79
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/internal/InstrumenterContext.java:35
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/internal/RuntimeVirtualFieldSupplier.java:54
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/internal/ServiceLoaderUtil.java:21
- [ ] instrumentation-api/src/main/java/io/opentelemetry/instrumentation/api/semconv/http/HttpServerRoute.java:157

## instrumentation-api-incubator

- [ ] instrumentation-api-incubator/src/main/java/io/opentelemetry/instrumentation/api/incubator/instrumenter/internal/InstrumenterCustomizerImpl.java:23

## instrumentation-docs

- [ ] instrumentation-docs/src/main/java/io/opentelemetry/instrumentation/docs/auditors/SuppressionListAuditor.java:102

## javaagent-tooling

- [ ] javaagent-tooling/src/main/java/io/opentelemetry/javaagent/tooling/field/RuntimeFieldBasedImplementationSupplier.java:36
- [ ] javaagent-tooling/src/main/java/io/opentelemetry/javaagent/tooling/util/ClassLoaderMap.java:67
- [ ] javaagent-tooling/src/main/java/io/opentelemetry/javaagent/tooling/util/ClassLoaderValue.java:30
- [ ] javaagent-tooling/src/main/java/io/opentelemetry/javaagent/tooling/util/ClassLoaderValue.java:39
- [ ] javaagent-tooling/src/main/java/io/opentelemetry/javaagent/tooling/util/TrieImpl.java:97
- [ ] javaagent-tooling/src/main/java/net/bytebuddy/agent/builder/AgentBuilderUtil.java:275
- [ ] javaagent-tooling/src/main/java/net/bytebuddy/agent/builder/AgentBuilderUtil.java:281
- [ ] javaagent-tooling/src/main/java/net/bytebuddy/agent/builder/AgentBuilderUtil.java:298

## smoke-tests

- [ ] smoke-tests/src/main/java/io/opentelemetry/smoketest/TelemetryRetriever.java:77

## smoke-tests-otel-starter/spring-boot-common

- [ ] smoke-tests-otel-starter/spring-boot-common/src/main/java/io/opentelemetry/spring/smoketest/AbstractJvmKafkaSpringStarterSmokeTest.java:68

## testing-common

- [ ] testing-common/src/main/java/io/opentelemetry/instrumentation/test/utils/ExceptionUtils.java:10
- [ ] testing-common/src/main/java/io/opentelemetry/instrumentation/test/utils/ExceptionUtils.java:19
- [ ] testing-common/src/main/java/io/opentelemetry/instrumentation/testing/junit/db/SemconvStabilityUtil.java:66
- [ ] testing-common/src/main/java/io/opentelemetry/instrumentation/testing/recording/RecordingExtension.java:28
- [ ] testing-common/src/main/java/io/opentelemetry/instrumentation/testing/util/ContextStorageCloser.java:88
- [ ] testing-common/src/main/java/io/opentelemetry/javaagent/testing/common/AgentTestingExporterAccess.java:105
- [ ] testing-common/src/main/java/io/opentelemetry/javaagent/testing/common/AgentTestingExporterAccess.java:126
- [ ] testing-common/src/main/java/io/opentelemetry/javaagent/testing/common/AgentTestingExporterAccess.java:82
- [ ] testing-common/src/main/java/io/opentelemetry/javaagent/testing/common/TestAgentListenerAccess.java:82

