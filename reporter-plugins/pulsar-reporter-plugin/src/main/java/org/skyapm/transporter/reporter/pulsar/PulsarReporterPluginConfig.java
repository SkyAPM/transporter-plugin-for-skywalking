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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.pulsar.common.naming.TopicDomain;
import org.apache.pulsar.common.naming.TopicName;
import org.apache.skywalking.apm.agent.core.boot.PluginConfig;

public class PulsarReporterPluginConfig {
    public static class Plugin {
        @PluginConfig(root = PulsarReporterPluginConfig.class)
        public static class Pulsar {
            /**
             * <B>Service URL provider for Pulsar service</B>: A list of host/port pairs to use for establishing the
             * initial connection to the pulsar broker. This list should be in the form
             * pulsar://localhost:6650,localhost:6651,localhost:6652,...
             */
            public static String BROKER_SERVICE_URL = "pulsar://localhost:6650";

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

            public static Map<String, Object> PULSAR_CLIENT_CONFIG = new HashMap<>();

            public static Map<String, Object> PRODUCER_DEFAULT_CONFIG = new HashMap<>();

            /**
             * Configurations of pulsar admin client
             */
            public static String WEB_SERVICE_URL = "http://localhost:8080";
            public static String ADMIN_AUTH_PLUGIN_CLASSNAME = null;
            public static Map<String, String> ADMIN_AUTH_PARAMS = new HashMap<>();
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
            public static Set<String> ADMIN_TLS_CIPHERS = new HashSet<>();
            public static Set<String> ADMIN_TLS_PROTOCOLS = new HashSet<>();
            public static String ADMIN_TLS_TRUST_STORE_PASSWORD = null;
            public static String ADMIN_TLS_TRUST_STORE_PATH = null;
            public static String ADMIN_TLS_TRUST_STORE_TYPE = "JKS";

            public static int PULSAR_CLIENT_SCHEDULE_AT_FIXED_RETE_INITIAL_DELAY = 0;
            public static int PULSAR_CLIENT_SCHEDULE_AT_FIXED_RETE_PERIOD = 120;
            public static TimeUnit PULSAR_CLIENT_SCHEDULE_AT_FIXED_RETE_UNIT = TimeUnit.SECONDS;
        }
    }
}
