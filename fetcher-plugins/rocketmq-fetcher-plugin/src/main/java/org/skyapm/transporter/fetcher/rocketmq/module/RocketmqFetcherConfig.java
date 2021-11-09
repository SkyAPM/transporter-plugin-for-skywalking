/*
 * Copyright 2021 SkyAPM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.skyapm.transporter.fetcher.rocketmq.module;

import lombok.Data;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;

import java.util.Properties;

@Data
public class RocketmqFetcherConfig extends ModuleConfig {
    /**
     * Rocketmq consumer config.
     */
    private Properties rocketmqConsumerConfig = new Properties();

    /**
     * rocketmq nameserver, This list should be in the form host1:port1,host2:port2,...
     */
    private String nameServers = "localhost:9876";

    /**
     * Rocketmq cluster name.
     */
    private String cluster = "DefaultCluster";

    /**
     * Rocketmq write queue nums.
     */
    private int queues = 3;

    /**
     * Rocketmq consumer group name.
     */
    private String consumerGroup = "Skywalking_consumer_group";

    /**
     * Rocketmq consumer pull Batch size.
     */
    private int pullBatchSize = 100;

    /**
     * Rocketmq consumer poll time out.
     */
    private long pollTimeoutMillis = 10000;

    private boolean enableNativeProtoLog = false;

    private boolean enableNativeJsonLog = false;

    private String configPath = "meter-analyzer-config";

    private String topicNameOfMetrics = "skywalking-metrics";

    private String topicNameOfProfiling = "skywalking-profilings";

    private String topicNameOfTracingSegments = "skywalking-segments";

    private String topicNameOfManagements = "skywalking-managements";

    private String topicNameOfMeters = "skywalking-meters";

    private String topicNameOfLogs = "skywalking-logs";

    private String topicNameOfJsonLogs = "skywalking-logs-json";

    private int rocketmqHandlerThreadPoolSize = -1;

    private int rocketmqHandlerThreadPoolQueueSize = -1;

    private String namespace = "";
}
