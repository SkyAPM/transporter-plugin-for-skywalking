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

package org.skyapm.transporter.reporter.rocketmq;

import org.apache.skywalking.apm.agent.core.boot.PluginConfig;

public class RocketmqReporterPluginConfig {
    public static class Plugin {
        @PluginConfig(root = RocketmqReporterPluginConfig.class)
        public static class Rocketmq {
            /**
             * rocketmq nameserver, This list should be in the form host1:port1,host2:port2,...
             */
            public static String NAME_SERVERS = "localhost:9876";

            public static String TOPIC_METRICS = "skywalking-metrics";

            public static String TOPIC_PROFILING = "skywalking-profilings";

            public static String TOPIC_SEGMENT = "skywalking-segments";

            public static String TOPIC_MANAGEMENT = "skywalking-managements";

            public static String TOPIC_METER = "skywalking-meters";

            public static String NAMESPACE = "";

            public static String PRODUCE_GROUP = "skywalking";

            /**
             * rocketmq vip channel enable. details : https://github.com/apache/rocketmq/issues/1510
             */
            public static Boolean VIP_ENABLE = false;

            /**
             * Timeout period of produce, the unit is millisecond.
             */
            public static long PRODUCE_TIMEOUT = 10000;

            /**
             * Client limit message size, over it may error. Server also limit so need to work with server
             */
            public static int MAX_MESSAGE_SIZE = 4000;

            /**
             * If send message failed, maximum number of retries.
             */
            public static int RETRY_TIMES = 2;

            /**
             * If send message and return sendResult but sendStatus!=SEND_OK, Whether to resend
             */
            public static boolean RETRY_ANOTHER_BROKER = false;
        }
    }
}
