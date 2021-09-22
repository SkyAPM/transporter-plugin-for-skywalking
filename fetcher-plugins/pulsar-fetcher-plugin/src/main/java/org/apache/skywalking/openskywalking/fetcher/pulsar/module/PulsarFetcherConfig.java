/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.skywalking.openskywalking.fetcher.pulsar.module;

import lombok.Data;
import org.apache.pulsar.common.naming.TopicDomain;
import org.apache.pulsar.common.naming.TopicName;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;

import java.util.HashMap;
import java.util.Map;

@Data
public class PulsarFetcherConfig extends ModuleConfig {
    /**
     * <B>bootstrap.servers</B>: A list of host/port pairs to use for establishing the initial connection to the pulsar
     * cluster. This list should be in the form pulsar://localhost:6650,localhost:6651,localhost:6652,...
     */
    private String bootstrapServers = "pulsar://localhost:6650";

    /**
     * pulsar consumer config.
     */
    private Map<String, Object> pulsarConsumerConfig = new HashMap<>();

    /**
     * pulsar client config.
     */
    private Map<String, Object> pulsarClientConfig = new HashMap<>();

    /**
     * <B>subscription</B>: the subscription name of consumption topics.
     */
    private String subscription = "skywalking-oap";

    private boolean enableNativeProtoLog = false;

    private boolean enableNativeJsonLog = false;

    private String topicNameOfMetrics = "skywalking-metrics";

    private String topicNameOfProfiling = "skywalking-profilings";

    private String topicNameOfTracingSegments = "skywalking-segments";

    private String topicNameOfManagements = "skywalking-managements";

    private String topicNameOfMeters = "skywalking-meters";

    private String topicNameOfLogs = "skywalking-logs";

    private String topicNameOfJsonLogs = "skywalking-logs-json";

    private int pulsarHandlerThreadPoolSize;

    private int pulsarHandlerThreadPoolQueueSize;

    /**
     * only tenant/namespace
     */
    private String namespace = TopicName.PUBLIC_TENANT + '/' + TopicName.DEFAULT_NAMESPACE;

    private String domain = TopicDomain.persistent.value();

}
