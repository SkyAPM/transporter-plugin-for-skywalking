# Pulsar Reporter
The Pulsar reporter plugin is based on `pulsar 2.8.0` and `Skywalking 8.7.0`. It supports report traces, JVM metrics, Instance Properties, and profiled snapshots to Pulsar cluster, which is disabled in default. Add the jar `pulsar-reporter-plugin-x.y.z.jar` to agent/plugins for activating. All configurations of the Pulsar Reporter plugin should be added to the file `agent.config`.

## Basic Configurations
Similar to Kafka reporter, GRPC receiver needs to be configured for delivering the task of profiling in the Pulsar Reporter. The following configure cannot be omitted.

```
# Backend service addresses.
collector.backend_service=${SW_AGENT_COLLECTOR_BACKEND_SERVICES:127.0.0.1:11800}

# Pulsar producer configuration
plugin.pulsar.client_service_url=${SW_PULSAR_CLIENT_SERVICE_URL:pulsar://localhost:6650}
plugin.pulsar.web_service_url=${SW_PULSAR_WEB_SERVICE_URL:http://localhost:8080}
```
For multiple brokers,
```
# Backend service addresses.
collector.backend_service=${SW_AGENT_COLLECTOR_BACKEND_SERVICES:127.0.0.1:11800}

# Pulsar producer configuration
plugin.pulsar.client_service_url=${SW_PULSAR_CLIENT_SERVICE_URL:pulsar://localhost:6650,localhost:6651,localhost:6652}
plugin.pulsar.web_service_url=${SW_PULSAR_WEB_SERVICE_URL:http://localhost:8080,localhost:8081,localhost:8082}
```
Before you activated the Pulsar reporter, you have to make sure that Pulsar fetcher of OAP server has been opened in service.
## Advanced Pulsar Configurations
### Client
The client configurations of Pulsar reporter are as follows. For detailed explanation of the configurations, please refer to [Pulsar client](https://pulsar.apache.org/docs/en/2.8.0/client-libraries-java/#client).
```
plugin.pulsar.client_auth_plugin_class_name=${SW_PULSAR_CLIENT_AUTH_PLUGIN_CLASS_NAME:"com.org.MyAuthPluginClass"}
plugin.pulsar.client_auth_params=${SW_PULSAR_CLIENT_AUTH_PARAMS:"key1:val1,key2:val2"}
plugin.pulsar.client_operation_timeout_ms=${SW_PULSAR_CLIENT_OPERATION_TIMEOUT_MS:30000}
plugin.pulsar.client_stats_interval_seconds=${SW_PULSAR_CLIENT_STATS_INTERVAL_SECONDS:60}
plugin.pulsar.client_num_io_threads=${SW_PULSAR_CLIENT_NUM_IO_THREADS:1}
plugin.pulsar.client_num_listener_threads=${SW_PULSAR_CLIENT_NUM_LISTENER_THREADS:1}
plugin.pulsar.CLIENT_USE_TCP_NO_DELAYCLIENT_USE_TCP_NO_DELAY:true}
plugin.pulsar.client_use_tls=${SW_PULSAR_CLIENT_USE_TLS:false}
plugin.pulsar.client_tls_trust_certs_file_path=${SW_PULSAR_CLIENT_TLS_TRUST_CERTS_FILE_PATH:"path"}
plugin.pulsar.client_tls_allow_insecure_connection=${SW_PULSAR_CLIENT_TLS_ALLOW_INSECURE_CONNECTION:false}
plugin.pulsar.client_tls_hostname_verification_enable=${SW_PULSAR_CLIENT_TLS_HOSTNAME_VERIFICATION_ENABLE:false}
plugin.pulsar.client_concurrent_lookup_request=${SW_PULSAR_CLIENT_CONCURRENT_LOOKUP_REQUEST:5000}
plugin.pulsar.client_max_lookup_request=${SW_PULSAR_CLIENT_MAX_LOOKUP_REQUEST:50000}
plugin.pulsar.client_max_number_of_rejected_request_per_connection=${SW_PULSAR_CLIENT_MAX_NUMBER_OF_REJECTED_REQUEST_PER_CONNECTION:50}
plugin.pulsar.client_keep_alive_interval_seconds=${SW_PULSAR_CLIENT_KEEP_ALIVE_INTERVAL_SECONDS:30}
plugin.pulsar.client_connection_timeout_ms=${SW_PULSAR_CLIENT_CONNECTION_TIMEOUT_MS:10000}
plugin.pulsar.client_request_timeout_ms=${SW_PULSAR_CLIENT_REQUEST_TIMEOUT_MS:60000}
plugin.pulsar.client_max_back_off_interval_nanos=${SW_PULSAR_CLIENT_MAX_BACK_OFF_INTERVAL_NANOS:30000000000}
```

### Admin Clinet Authentication Configurations
According to [Pulsar Admin](https://pulsar.apache.org/docs/en/2.8.0/admin-api-overview/), all authentication configurations are as follows.
```
plugin.pulsar.admin_auth_plugin_classname=${SW_PULSAR_ADMIN_AUTH_PLUGIN_CLASSNAME:"com.org.MyAuthPluginClass"}
plugin.pulsar.admin_auth_params=${SW_PULSAR_ADMIN_AUTH_PARAMS:"key1:val1,key2:val2"}
plugin.pulsar.admin_tls_allow_insecure_connection=${SW_PULSAR_ADMIN_TLS_ALLOW_INSECURE_CONNECTION:false}
plugin.pulsar.admin_tls_trust_certs_file_path=${SW_PULSAR_ADMIN_TLS_TRUST_CERTS_FILE_PATH:null}
plugin.pulsar.admin_auto_cert_refresh_time=${SW_PULSAR_ADMIN_AUTO_CERT_REFRESH_TIME:300}
plugin.pulsar.admin_connection_timeout=${SW_PULSAR_ADMIN_CONNECTION_TIMEOUT:60}
plugin.pulsar.admin_use_key_store_tls=${SW_PULSAR_ADMIN_USE_KEY_STORE_TLS:false}
plugin.pulsar.admin_enable_tls_host_name_verification=${SW_PULSAR_ADMIN_ENABLE_TLS_HOST_NAME_VERIFICATION:false}
plugin.pulsar.admin_read_timeout=${SW_PULSAR_ADMIN_READ_TIMEOUT:60}
plugin.pulsar.admin_request_timeout=${SW_PULSAR_ADMIN_REQUEST_TIMEOUT:300}
plugin.pulsar.admin_ssl_provider=${SW_PULSAR_ADMIN_SSL_PROVIDER:"provider"}
plugin.pulsar.admin_tls_ciphers=${SW_PULSAR_ADMIN_TLS_CIPHERS:"[TLS_DH_RSA_WITH_AES_256_GCM_SHA384,TLS_DH_RSA_WITH_AES_256_CBC_SHA]"}
plugin.pulsar.admin_tls_protocols=${SW_PULSAR_ADMIN_TLS_PROTOCOLS:"[TLSv1.2,TLSv1.1]"}
plugin.pulsar.admin_tls_trust_store_password=${SW_PULSAR_ADMIN_TLS_TRUST_STORE_PASSWORD:"password"}
plugin.pulsar.admin_tls_trust_store_path=${SW_PULSAR_ADMIN_TLS_TRUST_STORE_PATH:"path"}
plugin.pulsar.admin_tls_trust_store_type=${SW_PULSAR_ADMIN_TLS_TRUST_STORE_TYPE:"JKS"}
```
### Producer configurations
According to [Pulsar Producer](https://pulsar.apache.org/docs/en/2.8.0/client-libraries-java/#producer), all Producer configurations are as follows.
```
plugin.pulsar.producer_send_timeout_ms=${SW_PULSAR_PRODUCER_SEND_TIMEOUT_MS:30000}
plugin.pulsar.producer_block_if_queue_full=${SW_PULSAR_PRODUCER_BLOCK_IF_QUEUE_FULL:false}
plugin.pulsar.producer_max_pending_messages=${SW_PULSAR_PRODUCER_MAX_PENDING_MESSAGES:1000}
plugin.pulsar.producer_max_pending_messages_across_partitions=${SW_PULSAR_PRODUCER_MAX_PENDING_MESSAGES_ACROSS_PARTITIONS:50000}
plugin.pulsar.producer_message_routing_mode=${SW_PULSAR_PRODUCER_MESSAGE_ROUTING_MODE:"RoundRobinPartition"}
plugin.pulsar.producer_hashing_scheme=${SW_PULSAR_PRODUCER_HASHING_SCHEME:"JavaStringHash"}
plugin.pulsar.producer_crypto_failure_action=${SW_PULSAR_PRODUCER_CRYPTO_FAILURE_ACTION:"FAIL"}
plugin.pulsar.producer_batching_max_publish_delay_micros=${SW_PULSAR_PRODUCER_BATCHING_MAX_PUBLISH_DELAY_MICROS:1000}
plugin.pulsar.producer_batching_max_messages=${SW_PULSAR_PRODUCER_BATCHING_MAX_MESSAGES:1000}
plugin.pulsar.producer_batching_enabled=${SW_PULSAR_PRODUCER_BATCHING_ENABLED:true}
plugin.pulsar.producer_compression_type=${SW_PULSAR_PRODUCER_COMPRESSION_TYPE:"NONE"}
```
Note that producers of all topics share a set of configurations.
### Topic configurations
The complete topic format of pulasr is `{persistent|non-persistent}://tenant/namespace/topic`. in the Pulsar reporter plugin, the default configuration is `persistent://public/default/topic`.
You can use the followiing configurations to set the format.
```
plugin.pulsar.domain=${SW_PULSAR_DOMAIN:"persistent"}

# tenant/namespace or tenant/cluster/namespace
plugin.pulsar.namespace=${SW_PULSAR_NAMESPACE:"public/default"}
```