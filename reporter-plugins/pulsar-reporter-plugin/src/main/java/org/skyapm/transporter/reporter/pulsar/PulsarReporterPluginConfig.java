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

package org.skyapm.transporter.reporter.pulsar;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.pulsar.client.api.CompressionType;
import org.apache.pulsar.client.api.HashingScheme;
import org.apache.pulsar.client.api.MessageRoutingMode;
import org.apache.pulsar.client.api.ProducerCryptoFailureAction;
import org.apache.pulsar.common.naming.TopicDomain;
import org.apache.pulsar.common.naming.TopicName;
import org.apache.skywalking.apm.agent.core.boot.PluginConfig;

public class PulsarReporterPluginConfig {

    public static class Plugin {
        @PluginConfig(root = PulsarReporterPluginConfig.class)
        public static class Pulsar {
            public static String TOPIC_METRICS = "skywalking-metrics";

            public static String TOPIC_PROFILING = "skywalking-profilings";

            public static String TOPIC_SEGMENT = "skywalking-segments";

            public static String TOPIC_MANAGEMENT = "skywalking-managements";

            public static String TOPIC_METER = "skywalking-meters";

            /**
             * tenant/namespace or tenant/cluster/namespace
             */
            public static String NAMESPACE = TopicName.PUBLIC_TENANT + '/' + TopicName.DEFAULT_NAMESPACE;

            public static String DOMAIN = TopicDomain.persistent.value();

            /**
             * Configurations of pulsar client
             */
            /**
             * <B>Service URL provider for Pulsar service</B>: A list of host/port pairs to use for establishing the
             * initial connection to the pulsar broker. This list should be in the form
             * pulsar://localhost:6650,localhost:6651,localhost:6652,...
             */
            public static String CLIENT_SERVICE_URL = "pulsar://localhost:6650";
            public static String CLIENT_AUTH_PLUGIN_CLASS_NAME = null;
            public static String CLIENT_AUTH_PARAMS = null;
            public static long CLIENT_OPERATION_TIMEOUT_MS = 30000;
            public static long CLIENT_STATS_INTERVAL_SECONDS = 60;
            public static int CLIENT_NUM_IO_THREADS = 1;
            public static int CLIENT_NUM_LISTENER_THREADS = 1;
            public static boolean CLIENT_USE_TCP_NO_DELAY = true;
            public static boolean CLIENT_USE_TLS = false;
            public static String CLIENT_TLS_TRUST_CERTS_FILE_PATH = null;
            public static boolean CLIENT_TLS_ALLOW_INSECURE_CONNECTION = false;
            public static boolean CLIENT_TLS_HOSTNAME_VERIFICATION_ENABLE = false;
            public static int CLIENT_CONCURRENT_LOOKUP_REQUEST = 5000;
            public static int CLIENT_MAX_LOOKUP_REQUEST = 50000;
            public static int CLIENT_MAX_NUMBER_OF_REJECTED_REQUEST_PER_CONNECTION = 50;
            public static int CLIENT_KEEP_ALIVE_INTERVAL_SECONDS = 30;
            public static int CLIENT_CONNECTION_TIMEOUT_MS = 10000;
            public static int CLIENT_REQUEST_TIMEOUT_MS = 60000;
            public static long CLIENT_MAX_BACK_OFF_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(30);
            private static Map<String, Object> PULSAR_CLIENT_CONFIG = new HashMap();

            /**
             * Configurations of pulsar producer
             */
            public static long PRODUCER_SEND_TIMEOUT_MS = 30000;
            public static boolean PRODUCER_BLOCK_IF_QUEUE_FULL = false;
            public static int PRODUCER_MAX_PENDING_MESSAGES = 1000;
            public static int PRODUCER_MAX_PENDING_MESSAGES_ACROSS_PARTITIONS = 50000;
            public static String PRODUCER_MESSAGE_ROUTING_MODE = MessageRoutingMode.RoundRobinPartition.name();
            public static String PRODUCER_HASHING_SCHEME = HashingScheme.JavaStringHash.name();
            public static String PRODUCER_CRYPTO_FAILURE_ACTION = ProducerCryptoFailureAction.FAIL.name();
            public static long PRODUCER_BATCHING_MAX_PUBLISH_DELAY_MICROS = TimeUnit.MILLISECONDS.toMicros(1);
            public static int PRODUCER_BATCHING_MAX_MESSAGES = 1000;
            public static boolean PRODUCER_BATCHING_ENABLED = true;
            public static String PRODUCER_COMPRESSION_TYPE = CompressionType.NONE.name();
            private static Map<String, Object> PRODUCER_DEFAULT_CONFIG = new HashMap<>();

            /**
             * Configurations of pulsar admin client
             */
            public static String WEB_SERVICE_URL = "http://localhost:8080";
            public static String ADMIN_AUTH_PLUGIN_CLASSNAME = null;
            public static String ADMIN_AUTH_PARAMS = null;
            public static boolean ADMIN_TLS_ALLOW_INSECURE_CONNECTION = false;
            public static String ADMIN_TLS_TRUST_CERTS_FILE_PATH = null;
            public static ClassLoader ADMIN_CONTEXT_CLASSLOADER = null;
            public static int ADMIN_AUTO_CERT_REFRESH_TIME = 300;
            public static int ADMIN_CONNECTION_TIMEOUT = 60;
            public static boolean ADMIN_USE_KEY_STORE_TLS = false;
            public static boolean ADMIN_ENABLE_TLS_HOST_NAME_VERIFICATION = false;
            public static int ADMIN_READ_TIMEOUT = 60;
            public static int ADMIN_REQUEST_TIMEOUT = 300;
            public static String ADMIN_SSL_PROVIDER = null;
            public static String ADMIN_TLS_CIPHERS = null;
            public static String ADMIN_TLS_PROTOCOLS = null;
            public static String ADMIN_TLS_TRUST_STORE_PASSWORD = null;
            public static String ADMIN_TLS_TRUST_STORE_PATH = null;
            public static String ADMIN_TLS_TRUST_STORE_TYPE = "JKS";

            public static int PULSAR_CLIENT_SCHEDULE_AT_FIXED_RETE_INITIAL_DELAY = 0;
            public static int PULSAR_CLIENT_SCHEDULE_AT_FIXED_RETE_PERIOD = 120;
            public static TimeUnit PULSAR_CLIENT_SCHEDULE_AT_FIXED_RETE_UNIT = TimeUnit.SECONDS;

            public static Map<String, Object> getPulsarClientConfigMap() {
                if (PULSAR_CLIENT_CONFIG.isEmpty()) {
                    PULSAR_CLIENT_CONFIG.put("serviceUrl", CLIENT_SERVICE_URL);
                    PULSAR_CLIENT_CONFIG.put("authPluginClassName", CLIENT_AUTH_PLUGIN_CLASS_NAME);
                    PULSAR_CLIENT_CONFIG.put("authParams", CLIENT_AUTH_PARAMS);
                    PULSAR_CLIENT_CONFIG.put("operationTimeoutMs", CLIENT_OPERATION_TIMEOUT_MS);
                    PULSAR_CLIENT_CONFIG.put("statsIntervalSeconds", CLIENT_STATS_INTERVAL_SECONDS);
                    PULSAR_CLIENT_CONFIG.put("numIoThreads", CLIENT_NUM_IO_THREADS);
                    PULSAR_CLIENT_CONFIG.put("numListenerThreads", CLIENT_NUM_LISTENER_THREADS);
                    PULSAR_CLIENT_CONFIG.put("useTcpNoDelay", CLIENT_USE_TCP_NO_DELAY);
                    PULSAR_CLIENT_CONFIG.put("useTls", CLIENT_USE_TLS);
                    PULSAR_CLIENT_CONFIG.put("tlsTrustCertsFilePath", CLIENT_TLS_TRUST_CERTS_FILE_PATH);
                    PULSAR_CLIENT_CONFIG.put("tlsAllowInsecureConnection", CLIENT_TLS_ALLOW_INSECURE_CONNECTION);
                    PULSAR_CLIENT_CONFIG.put("tlsHostnameVerificationEnable", CLIENT_TLS_HOSTNAME_VERIFICATION_ENABLE);
                    PULSAR_CLIENT_CONFIG.put("concurrentLookupRequest", CLIENT_CONCURRENT_LOOKUP_REQUEST);
                    PULSAR_CLIENT_CONFIG.put("maxLookupRequest", CLIENT_MAX_LOOKUP_REQUEST);
                    PULSAR_CLIENT_CONFIG.put(
                        "maxNumberOfRejectedRequestPerConnection",
                        CLIENT_MAX_NUMBER_OF_REJECTED_REQUEST_PER_CONNECTION
                    );
                    PULSAR_CLIENT_CONFIG.put("keepAliveIntervalSeconds", CLIENT_KEEP_ALIVE_INTERVAL_SECONDS);
                    PULSAR_CLIENT_CONFIG.put("connectionTimeoutMs", CLIENT_CONNECTION_TIMEOUT_MS);
                    PULSAR_CLIENT_CONFIG.put("requestTimeoutMs", CLIENT_REQUEST_TIMEOUT_MS);
                    PULSAR_CLIENT_CONFIG.put("maxBackoffIntervalNanos", CLIENT_MAX_BACK_OFF_INTERVAL_NANOS);
                }
                return PULSAR_CLIENT_CONFIG;
            }

            public static Map<String, Object> getProducerDefaultConfig() {
                if (PRODUCER_DEFAULT_CONFIG.isEmpty()) {
                    PRODUCER_DEFAULT_CONFIG.put("sendTimeoutMs", PRODUCER_SEND_TIMEOUT_MS);
                    PRODUCER_DEFAULT_CONFIG.put("blockIfQueueFull", PRODUCER_BLOCK_IF_QUEUE_FULL);
                    PRODUCER_DEFAULT_CONFIG.put("maxPendingMessages", PRODUCER_MAX_PENDING_MESSAGES);
                    PRODUCER_DEFAULT_CONFIG.put("maxPendingMessagesAcrossPartitions",
                                                PRODUCER_MAX_PENDING_MESSAGES_ACROSS_PARTITIONS);
                    PRODUCER_DEFAULT_CONFIG.put("messageRoutingMode",
                                                MessageRoutingMode.valueOf(PRODUCER_MESSAGE_ROUTING_MODE));
                    PRODUCER_DEFAULT_CONFIG.put("hashingScheme", HashingScheme.valueOf(PRODUCER_HASHING_SCHEME));
                    PRODUCER_DEFAULT_CONFIG.put("cryptoFailureAction",
                                                ProducerCryptoFailureAction.valueOf(PRODUCER_CRYPTO_FAILURE_ACTION));
                    PRODUCER_DEFAULT_CONFIG.put("batchingMaxPublishDelayMicros",
                                                PRODUCER_BATCHING_MAX_PUBLISH_DELAY_MICROS);
                    PRODUCER_DEFAULT_CONFIG.put("batchingMaxMessages", PRODUCER_BATCHING_MAX_MESSAGES);
                    PRODUCER_DEFAULT_CONFIG.put("batchingEnabled", PRODUCER_BATCHING_ENABLED);
                    PRODUCER_DEFAULT_CONFIG.put("compressionType", CompressionType.valueOf(PRODUCER_COMPRESSION_TYPE));
                }
                return PRODUCER_DEFAULT_CONFIG;
            }
        }
    }
}