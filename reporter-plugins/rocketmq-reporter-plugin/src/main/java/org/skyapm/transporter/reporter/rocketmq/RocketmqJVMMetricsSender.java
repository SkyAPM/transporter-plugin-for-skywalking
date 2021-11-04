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

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.skywalking.apm.agent.core.boot.OverrideImplementor;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import org.apache.skywalking.apm.agent.core.conf.Config;
import org.apache.skywalking.apm.agent.core.jvm.JVMMetricsSender;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.network.language.agent.v3.JVMMetric;
import org.apache.skywalking.apm.network.language.agent.v3.JVMMetricCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A report to send JVM Metrics data rocketmq cluster.
 */
@OverrideImplementor(JVMMetricsSender.class)
public class RocketmqJVMMetricsSender  extends JVMMetricsSender implements RocketmqConnectionStatusListener {
    private static final ILog LOGGER = LogManager.getLogger(RocketmqJVMMetricsSender.class);
    private DefaultMQProducer producer;
    private String topic;
    private BlockingQueue<JVMMetric> queue;

    @Override
    public void run() {
        if (!queue.isEmpty()) {
            List<JVMMetric> buffer = new ArrayList<>();
            queue.drainTo(buffer);

            if (producer != null) {
                JVMMetricCollection metrics = JVMMetricCollection.newBuilder()
                        .addAllMetrics(buffer)
                        .setService(Config.Agent.SERVICE_NAME)
                        .setServiceInstance(Config.Agent.INSTANCE_NAME)
                        .build();

                if (LOGGER.isDebugEnable()) {
                    LOGGER.debug(
                            "JVM metrics reporting, topic: {}, key: {}, length: {}", topic, metrics.getServiceInstance(),
                            buffer.size()
                    );
                }
                try {
                    producer.send(new Message(
                            topic,
                            metrics.toByteArray()),
                            RocketmqReporterPluginConfig.Plugin.Rocketmq.PRODUCE_TIMEOUT);
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.error("Failed to report JVMMetrics.", e);
                }
            }
        }
    }

    @Override
    public void prepare() {
        queue = new LinkedBlockingQueue<>(Config.Jvm.BUFFER_SIZE);
        RocketmqProducerManager producerManager = ServiceManager.INSTANCE.findService(RocketmqProducerManager.class);
        producerManager.addListener(this);
        topic = producerManager.formatTopicNameThenRegister(RocketmqReporterPluginConfig.Plugin.Rocketmq.TOPIC_METRICS);
    }

    @Override
    public void boot() {
    }

    @Override
    public void offer(final JVMMetric metric) {
        if (!queue.offer(metric)) {
            queue.poll();
            queue.offer(metric);
        }
    }

    @Override
    public void onStatusChanged(RocketmqConnectionStatus status) {
        if (status == RocketmqConnectionStatus.CONNECTED) {
            producer = ServiceManager.INSTANCE.findService(RocketmqProducerManager.class).getProducer();
        }
    }
}
