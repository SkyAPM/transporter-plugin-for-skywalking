# Pulsar Fetcher
The Pulsar Fetcher plugin is based on `pulsar 2.8.0` and `Skywalking 8.7.0`. In Pulsar Fetcher, tracing segments, service/instance properties, JVM metrics, and meter system data are supported. Pulsar Fetcher can work with gRPC/HTTP Receivers at the same time for adopting different transport protocols.

Pulsar Fetcher is disabled by default. To enable it, you need to move the jar `pulsar-fetcher-plugin-x.y.z.jar` to `oap-libs/` and configure as follows. All configurations of the Pulsar Fetcher plugin should be added to the file `application.yml`.
```
pulsar-fetcher:
  selector: ${SW_PULSAR_FETCHER:default}
  default:
    clientServiceUrl: ${SW_PULSAR_FETCHER_SERVERS:"pulsar://pulsarhost:6650"}
```
`skywalking-segments`, `skywalking-metrics`, `skywalking-profilings`, `skywalking-managements`, `skywalking-meters`, `skywalking-logs` and `skywalking-logs-json` topics are required by pulsar-fetcher. If they do not exist, Pulsar Fetcher will create them by default. Also, you can create them by yourself before the OAP server starts.
## Advanced Pulsar Configurations
### Client
According to [Pulsar client](https://pulsar.apache.org/docs/en/2.8.0/client-libraries-java/#client), all client configurations are as follows.
```
pulsar-fetcher:
  selector: ${SW_PULSAR_FETCHER:default}
  default:
  ...
    clientAuthPluginClassName: ${SW_PULSAR_FETCHER_CLIENT_AUTH_PLUGIN_CLASS_NAME:"com.org.MyAuthPluginClass"}
    clientAuthParams: ${SW_PULSAR_FETCHER_CLIENT_AUTH_PARAMS:"key1:val1,key2:val2"}
    clientOperationTimeoutMs: ${SW_PULSAR_FETCHER_CLIENT_OPERATION_TIMEOUT_MS:30000}
    clientStatsIntervalSeconds: ${SW_PULSAR_FETCHER_CLIENT_STATS_INTERVAL_SECONDS:60}
    clientNumIoThreads: ${SW_PULSAR_FETCHER_CLIENT_NUM_IO_THREADS:1}
    clientNumListenerThreads: ${SW_PULSAR_FETCHER_CLIENT_NUM_LISTENER_THREADS:1}
    clientUseTcpNoDelay: ${SW_PULSAR_FETCHER_CLIENT_USE_TCP_NO_DELAY:true}
    clientUseTls: ${SW_PULSAR_FETCHER_CLIENT_USE_TLS:false}
    clientTlsTrustCertsFilePath: ${SW_PULSAR_FETCHER_CLIENT_TLS_TRUST_CERTS_FILE_PATH:"path"}
    clientTlsAllowInsecureConnection: ${SW_PULSAR_FETCHER_CLIENT_TLS_ALLOW_INSECURE_CONNECTION:false}
    clientTlsHostnameVerificationEnable: ${SW_PULSAR_FETCHER_CLIENT_TLS_HOSTNAME_VERIFICATION_ENABLE:false}
    clientConcurrentLookupRequest: ${SW_PULSAR_FETCHER_CLIENT_CONCURRENT_LOOKUP_REQUEST:5000}
    clientMaxLookupRequest: ${SW_PULSAR_FETCHER_CLIENT_MAX_LOOKUP_REQUEST:50000}
    clientMaxNumberOfRejectedRequestPerConnection: ${SW_PULSAR_FETCHER_CLIENT_MAX_NUMBER_OF_REJECTED_REQUEST_PER_CONNECTION:50}
    clientKeepAliveIntervalSeconds: ${SW_PULSAR_FETCHER_CLIENT_KEEP_ALIVE_INTERVAL_SECONDS:30}
    clientConnectionTimeoutMs: ${SW_PULSAR_FETCHER_CLIENT_CONNECTION_TIMEOUT_MS:10000}
    clientRequestTimeoutMs: ${SW_PULSAR_FETCHER_CLIENT_REQUEST_TIMEOUT_MS:60000}
    clientMaxBackOffIntervalNanos: ${SW_PULSAR_FETCHER_CLIENT_MAX_BACK_OFF_INTERVAL_NANOS:30000000000}
  ...  
```
### Consumer configurations
According to [Pulsar consumer](https://pulsar.apache.org/docs/en/2.8.0/client-libraries-java/#consumer), all consumer configurations are as follows.
```
pulsar-fetcher:
  selector: ${SW_PULSAR_FETCHER:default}
  default:
  ...
    subscriptionName: ${SW_PULSAR_FETCHER_SUBSCRIPTION_NAME:"skywalking-oap"}
    subscriptionType: ${SW_PULSAR_FETCHER_SUBSCRIPTION_TYPE:"Exclusive"}
    receiverQueueSize: ${SW_PULSAR_FETCHER_RECEIVER_QUEUE_SIZE:1000}
    acknowledgementsGroupTimeMicros: ${SW_PULSAR_FETCHER_ACKNOWLEDGEMENTS_GROUP_TIME_MICROS:100000}
    negativeAckRedeliveryDelayMicros: ${SW_PULSAR_FETCHER_NEGATIVE_ACK_REDELIVERY_DELAY_MICROS:60000000}
    maxTotalReceiverQueueSizeAcrossPartitions: ${SW_PULSAR_FETCHER_MAX_TOTAL_RECEIVER_QUEUE_SIZE_ACROSS_PARTITIONS:50000}
    consumerName: ${SW_PULSAR_FETCHER_CONSUMER_NAME:"name"}
    ackTimeoutMillis: ${SW_PULSAR_FETCHER_ACK_TIMEOUT_MILLIS:0}
    tickDurationMillis: ${SW_PULSAR_FETCHER_TICK_DURATION_MILLIS:1000}
    priorityLevel: ${SW_PULSAR_FETCHER_PRIORITY_LEVEL:0}
    cryptoFailureAction: ${SW_PULSAR_FETCHER_CRYPTO_FAILURE_ACTION:"FAIL"}
    properties: ${SW_PULSAR_FETCHER_PROPERTIES:"{\"k1\":\"v1\",\"k2\":\"v2\"}"}
    readCompacted: ${SW_PULSAR_FETCHER_READ_COMPACTED:false}
    subscriptionInitialPosition: ${SW_PULSAR_FETCHER_SUBSCRIPTION_INITIAL_POSITION:"Latest"}
    patternAutoDiscoveryPeriod: ${SW_PULSAR_FETCHER_PATTERN_AUTO_DISCOVERY_PERIOD:1}
    regexSubscriptionMode: ${SW_PULSAR_FETCHER_REGEX_SUBSCRIPTION_MODE:"PersistentOnly"}
    deadLetterPolicy: ${SW_PULSAR_FETCHER_DEAD_LETTER_POLICY:"{\"maxRedeliverCount\":0,\"retryLetterTopic\":\"retryTopic\",\"deadLetterTopic\":\"deadTopic\"}"}
    autoUpdatePartitions: ${SW_PULSAR_FETCHER_AUTO_UPDATE_PARTITIONS:true}
    replicateSubscriptionState: ${SW_PULSAR_FETCHER_REPLICATE_SUBSCRIPTION_STATE:false}
  ...
```
### Topic configurations
The complete topic format of pulasr is `{persistent|non-persistent}://tenant/namespace/topic`. in the Pulsar reporter plugin, the default configuration is `persistent://public/default/topic`.
You can use the followiing configurations to set the format.
```
pulsar-fetcher:
  selector: ${SW_PULSAR_FETCHER:default}
  default:
  ...
  # only tenant/namespace
    namespace: ${SW_PULSAR_FETCHER_NAMESPACE:"public/default"}
    domain: ${SW_PULSAR_FETCHER_DOMAIN:"persistent"}
```