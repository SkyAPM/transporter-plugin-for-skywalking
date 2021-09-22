/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.skywalking.openskywalking.reporter.pulsar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.skywalking.apm.agent.core.boot.OverrideImplementor;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import org.apache.skywalking.apm.agent.core.conf.Config;
import org.apache.skywalking.apm.agent.core.jvm.JVMMetricsSender;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.network.language.agent.v3.JVMMetric;
import org.apache.skywalking.apm.network.language.agent.v3.JVMMetricCollection;

/**
 * A report to send JVM Metrics data to Kafka Broker.
 */
@OverrideImplementor(JVMMetricsSender.class)
public class PulsarJVMMetricsSender extends JVMMetricsSender implements PulsarConnectionStatusListener {
    private static final ILog LOGGER = LogManager.getLogger(PulsarJVMMetricsSender.class);
    private Producer<byte[]> producer;

    private String topic;
    private BlockingQueue<JVMMetric> queue;

    @Override
    public void prepare() {
        queue = new LinkedBlockingQueue<>(Config.Jvm.BUFFER_SIZE);
        PulsarProducerManager producerManager = ServiceManager.INSTANCE.findService(PulsarProducerManager.class);
        producerManager.addListener(this);
        topic = producerManager.formatTopicNameThenRegister(PulsarReporterPluginConfig.Plugin.Pulsar.TOPIC_METRICS);
    }

    @Override
    public void boot() {
    }

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
                    producer.newMessage()
                            .key(metrics.getServiceInstance())
                            .value(metrics.toByteArray())
                            .send();
                    producer.flush();
                } catch (PulsarClientException e) {
                    LOGGER.error(e, "Send JVM metrics failed.");
                }
            }
        }
    }

    @Override
    public void offer(final JVMMetric metric) {
        if (!queue.offer(metric)) {
            queue.poll();
            queue.offer(metric);
        }
    }

    @Override
    public void onStatusChanged(PulsarConnectionStatus status) {
        if (status == PulsarConnectionStatus.CONNECTED) {
            producer = ServiceManager.INSTANCE.findService(PulsarProducerManager.class)
                                              .getProducer(topic);
            if (producer == null) {
                LOGGER.error("cannot get the producer.");
            }
        }
    }
}
