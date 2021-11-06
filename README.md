# Apache SkyWalking Transporter Plugin

This repo hosts plugins as the optional transporter implementations to replace the default communication machenism between SkyWalking Java agent and OAP server.
The Apache SkyWalking officially provides gRPC and Kafka as official transporters. Here are more

## License
Apache 2.0
## Available Transporter Plugins
* Pulsar Transporter(SkyWalking OAP server and Java agent 8.7.0).

  The current Pulsar Transporter plugin is based on `pulsar 2.8.0` and `Skywalking 8.7.0`.
    
    * [OAP Fetcher](docs/en/pulsar/Pulsar-Reporter.md)
    * [Java agent reporter](docs/en/pulsar/Pulsar-Fetcher.md)
