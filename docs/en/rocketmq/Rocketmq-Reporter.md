The Rocketmq reporter plugin  is based on `Rocketmq 4.7.1` and `Skywalking 8.7.0`,it support report traces, JVM metrics, Instance Properties, and profiled snapshots to Rocketmq cluster, which is disabled in default. Move the jar of the plugin, `rocketmq-reporter-plugin-x.y.z.jar`, to `agent/plugins` for activating.

Notice, currently, the agent still needs to configure GRPC receiver for delivering the task of profiling. In other words, the following configure cannot be omitted.

```properties
# Backend service addresses.
collector.backend_service=${SW_AGENT_COLLECTOR_BACKEND_SERVICES:127.0.0.1:11800}

# Rocketmq producer configuration
plugin.rocketmq.name_servers=${SW_ROCKETMQ_NAME_SERVERS:localhost:9876}
plugin.rocketmq.produer_group=${SW_ROCKETMQ_PRODUCE_GROUP:"skywalking"}
plugin.rocketmq.produce_timeout=${SW_ROCKETMQ_PRODUCE_TIMEOUT:10000}
plugin.rocketmq.vip_enable=${SW_ROCKETMQ_VIP_ENABLE:false}
plugin.rocketmq.max_message_size=${SW_ROCKETMQ_MAX_MESSAGE_SIZE:4000}
plugin.rocketmq.retry_times={SW_ROCKETMQ_RETRY_TIMES:2}
plugin.rocketmq.set_retry_another_broker={SW_ROCKETMQ_RETRY_ANOTHER_BROKER:false}
```

Rocketmq reporter plugin support to the configurations of listed, you can find explanation [here] [here](https://github.com/apache/rocketmq/blob/master/docs/en/Configuration_Client.md).

Before you activated the Rocketmq reporter, you have to make sure that [Rocketmq fetcher](./Rocketmq-fetcher.md) has been opened in service.
