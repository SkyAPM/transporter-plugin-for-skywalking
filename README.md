# Apache SkyWalking Transporter Plugin

This repo hosts plugins as the optional transporter implementations to replace the default communication machenism between SkyWalking Java agent and OAP server.
The Apache SkyWalking officially provides gRPC and Kafka as official transporters. Here are more

## Available Transporter Plugins
* Pulsar Transporter

  The current Pulsar Transporter plugin is based on `pulsar 2.8.0` and `SkyWalking OAP server and Java Agent 8.7.0`.
    
    * [OAP fetcher](docs/en/pulsar/Pulsar-Fetcher.md)
    * [Java agent reporter](docs/en/pulsar/Pulsar-Reporter.md)

* Rocketmq Transporter

  The current Rocketmq Transporter plugin is based on `rocketmq 4.7.1` and `SkyWalking OAP server and Java Agent 8.7.0`.
    
    * [OAP fetcher](docs/en/rocketmq/Rocketmq-Fetcher.md)
    * [Java agent reporter](docs/en/rocketmq/Rocketmq-Reporter.md)
    
## License
Apache 2.0
