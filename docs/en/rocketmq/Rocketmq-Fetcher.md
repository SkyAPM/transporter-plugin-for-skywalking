# Rocketmq Fetcher

The Rocketmq Fetcher is based on `Rocketmq 4.7.1` and `Skywalking 8.7.0`. it pulls messages from the Rocketmq cluster to learn about what agent is delivered. Check the agent documentation for details. Typically, tracing segments, service/instance properties, JVM metrics, and meter system data are supported.  Rocketmq Fetcher can work with gRPC/HTTP Receivers at the same time for adopting different transport protocols.

Rocketmq Fetcher is disabled by default. To enable it, configure as follows.

Namespace aims to isolate multi OAP cluster when using the same Rocketmq cluster.
If you set a namespace for Rocketmq fetcher, the OAP will add a prefix to topic name. You should also set namespace in the property named `plugin.Rocketmq.namespace` in `agent.config`.

```yaml
rocketmq-fetcher:
  selector: ${SW_ROCKETMQ_FETCHER:default}
  default:
    nameServers: ${SW_ROCKETMQ_FETCHER_NAME_SERVERS:localhost:9876}
    namespace: ${SW_NAMESPACE:""}
    cluster: ${SW_CLUSTER:"DefaultCluster"}
    queues: ${SW_ROCKETMQ_FETCHER_QUEUES:4}
    consumerGroup: ${SW_ROCKETMQ_CONSUMER_GROUP:"Skywalking_consumer_group"}
    pullBatchSize: ${SW_ROCKETMQ_PULL_BATCH_SIZE:100}
    pollTimeoutMillis: ${SW_ROCKETMQ_POLL_TIMEOUT_MILLIS:10000}
    enableNativeProtoLog: ${SW_ROCKETMQ_FETCHER_ENABLE_NATIVE_PROTO_LOG:false}
    enableNativeJsonLog: ${SW_ROCKETMQ_FETCHER_ENABLE_NATIVE_JSON_LOG:false}
    rocketmqHandlerThreadPoolSize: ${SW_ROCKETMQ_HANDLER_THREAD_POOL_SIZE:-1}
    rocketmqHandlerThreadPoolQueueSize: ${SW_ROCKETMQ_HANDLER_THREAD_POOL_QUEUE_SIZE:-1}
```

`skywalking-segments`, `skywalking-metrics`, `skywalking-profilings`, `skywalking-managements`, `skywalking-meters`, `skywalking-logs`
and `skywalking-logs-json` topics are required by `Rocketmq-fetcher`.
If they do not exist, you can create them by yourself before the OAP server starts. If you don't create topic, Rocketmq Fetcher will create them by config blew. 
you can find explanation [here](https://github.com/apache/rocketmq/blob/master/docs/en/Configuration_Client.md)
```yaml
rocketmq-fetcher:
  selector: ${SW_ROCKETMQ_FETCHER:default}
  default:
    nameServers: ${SW_ROCKETMQ_FETCHER_NAME_SERVERS:localhost:9876}
    cluster: ${SW_CLUSTER:"DefaultCluster"}
    queues: ${SW_ROCKETMQ_FETCHER_QUEUES:4}
    consumerGroup: ${SW_ROCKETMQ_CONSUMER_GROUP:"Skywalking_consumer_group"}
    pullBatchSize: ${SW_ROCKETMQ_PULL_BATCH_SIZE:100}
    pollTimeoutMillis: ${SW_ROCKETMQ_POLL_TIMEOUT_MILLIS:10000}
```