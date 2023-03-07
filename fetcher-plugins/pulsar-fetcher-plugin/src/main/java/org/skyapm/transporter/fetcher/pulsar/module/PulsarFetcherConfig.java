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

package org.skyapm.transporter.fetcher.pulsar.module;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.RegexSubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.common.naming.TopicDomain;
import org.apache.pulsar.common.naming.TopicName;
import org.apache.skywalking.oap.server.library.util.StringUtil;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;

@Data
public class PulsarFetcherConfig extends ModuleConfig {
    private static final Gson GSON = new Gson();

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

    /**
     * pulsar client config.
     */
    private String clientAuthPluginClassName = null;
    /**
     * <B>service.url</B>: A list of host/port pairs to use for establishing the initial connection to the pulsar
     * cluster. This list should be in the form pulsar://localhost:6650,localhost:6651,localhost:6652,...
     */
    private String clientServiceUrl = "pulsar://localhost:6650";
    private String clientAuthParams = null;
    private long clientOperationTimeoutMs = 30000;
    private long clientStatsIntervalSeconds = 60;
    private int clientNumIoThreads = 1;
    private int clientNumListenerThreads = 1;
    private boolean clientUseTcpNoDelay = true;
    private boolean clientUseTls = false;
    private String clientTlsTrustCertsFilePath = null;
    private boolean clientTlsAllowInsecureConnection = false;
    private boolean clientTlsHostnameVerificationEnable = false;
    private int clientConcurrentLookupRequest = 5000;
    private int clientMaxLookupRequest = 50000;
    private int clientMaxNumberOfRejectedRequestPerConnection = 50;
    private int clientKeepAliveIntervalSeconds = 30;
    private int clientConnectionTimeoutMs = 10000;
    private int clientRequestTimeoutMs = 60000;
    private long clientMaxBackOffIntervalNanos = TimeUnit.SECONDS.toNanos(30);

    private Map<String, Object> pulsarClientConfig = new HashMap<>();

    /**
     * consumer config
     */
    private String subscriptionName = "skywalking-oap";
    private String subscriptionType = SubscriptionType.Exclusive.name();
    private int receiverQueueSize = 1000;
    private long acknowledgementsGroupTimeMicros = TimeUnit.MILLISECONDS.toMicros(100);
    private long negativeAckRedeliveryDelayMicros = TimeUnit.MINUTES.toMicros(1);
    private int maxTotalReceiverQueueSizeAcrossPartitions = 50000;
    private String consumerName = null;
    private long ackTimeoutMillis = 0;
    private long tickDurationMillis = 1000;
    private int priorityLevel = 0;
    private String cryptoFailureAction = ConsumerCryptoFailureAction.FAIL.name();
    private String properties = null;
    private boolean readCompacted = false;
    private String subscriptionInitialPosition = SubscriptionInitialPosition.Latest.name();
    private int patternAutoDiscoveryPeriod = 1;
    private String regexSubscriptionMode = RegexSubscriptionMode.PersistentOnly.name();
    private String deadLetterPolicy = null;
    private boolean autoUpdatePartitions = true;
    private boolean replicateSubscriptionState = false;

    public Map<String, Object> getPulsarClientConfig() {
        Map<String, Object> pulsarClientConfig = new HashMap<>();
        pulsarClientConfig.put("serviceUrl", clientServiceUrl);
        pulsarClientConfig.put("authPluginClassName", clientAuthPluginClassName);
        pulsarClientConfig.put("authParams", clientAuthParams);
        pulsarClientConfig.put("operationTimeoutMs", clientOperationTimeoutMs);
        pulsarClientConfig.put("statsIntervalSeconds", clientStatsIntervalSeconds);
        pulsarClientConfig.put("numIoThreads", clientNumIoThreads);
        pulsarClientConfig.put("numListenerThreads", clientNumListenerThreads);
        pulsarClientConfig.put("useTcpNoDelay", clientUseTcpNoDelay);
        pulsarClientConfig.put("useTls", clientUseTls);
        pulsarClientConfig.put("tlsTrustCertsFilePath", clientTlsTrustCertsFilePath);
        pulsarClientConfig.put("tlsAllowInsecureConnection", clientTlsAllowInsecureConnection);
        pulsarClientConfig.put("tlsHostnameVerificationEnable", clientTlsHostnameVerificationEnable);
        pulsarClientConfig.put("concurrentLookupRequest", clientConcurrentLookupRequest);
        pulsarClientConfig.put("maxLookupRequest", clientMaxLookupRequest);
        pulsarClientConfig.put(
            "maxNumberOfRejectedRequestPerConnection",
            clientMaxNumberOfRejectedRequestPerConnection
        );
        pulsarClientConfig.put("keepAliveIntervalSeconds", clientKeepAliveIntervalSeconds);
        pulsarClientConfig.put("connectionTimeoutMs", clientConnectionTimeoutMs);
        pulsarClientConfig.put("requestTimeoutMs", clientRequestTimeoutMs);
        pulsarClientConfig.put("maxBackoffIntervalNanos", clientMaxBackOffIntervalNanos);

        return pulsarClientConfig;
    }

    public Map<String, Object> getPulsarConsumerConfig() {
        Map<String, Object> pulsarConsumerConfig = new HashMap<>();
        pulsarConsumerConfig.put("subscriptionName", subscriptionName);
        pulsarConsumerConfig.put("subscriptionType", SubscriptionType.valueOf(subscriptionType));
        pulsarConsumerConfig.put("receiverQueueSize", receiverQueueSize);
        pulsarConsumerConfig.put("acknowledgementsGroupTimeMicros", acknowledgementsGroupTimeMicros);
        pulsarConsumerConfig.put("negativeAckRedeliveryDelayMicros", negativeAckRedeliveryDelayMicros);
        pulsarConsumerConfig.put(
            "maxTotalReceiverQueueSizeAcrossPartitions",
            maxTotalReceiverQueueSizeAcrossPartitions
        );
        pulsarConsumerConfig.put("consumerName", consumerName);
        pulsarConsumerConfig.put("ackTimeoutMillis", ackTimeoutMillis);
        pulsarConsumerConfig.put("tickDurationMillis", tickDurationMillis);
        pulsarConsumerConfig.put("priorityLevel", priorityLevel);
        pulsarConsumerConfig.put("cryptoFailureAction", ConsumerCryptoFailureAction.valueOf(cryptoFailureAction));

        Type type = new TypeToken<TreeMap<String, String>>() {
        }.getType();
        pulsarConsumerConfig.put("properties", GSON.fromJson(properties, type));

        pulsarConsumerConfig.put("readCompacted", readCompacted);
        pulsarConsumerConfig.put(
            "subscriptionInitialPosition",
            SubscriptionInitialPosition.valueOf(subscriptionInitialPosition)
        );
        pulsarConsumerConfig.put("patternAutoDiscoveryPeriod", patternAutoDiscoveryPeriod);
        pulsarConsumerConfig.put("regexSubscriptionMode", RegexSubscriptionMode.valueOf(regexSubscriptionMode));

        if (StringUtil.isNotBlank(deadLetterPolicy)) {
            pulsarConsumerConfig.put("deadLetterPolicy", GSON.fromJson(deadLetterPolicy, DeadLetterPolicy.class));
        }

        pulsarConsumerConfig.put("autoUpdatePartitions", autoUpdatePartitions);
        pulsarConsumerConfig.put("replicateSubscriptionState", replicateSubscriptionState);

        return pulsarConsumerConfig;
    }

}
