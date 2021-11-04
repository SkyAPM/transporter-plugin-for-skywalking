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
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.profile.ProfileSnapshotSender;
import org.apache.skywalking.apm.agent.core.profile.TracingThreadSnapshot;
import org.apache.skywalking.apm.network.language.profile.v3.ThreadSnapshot;

import java.util.List;

/**
 * To transport profiling tasks between OAP Server and agent with gRPC. This is why we still have to configure gRPC. But
 * to report the tracing profile snapshot data by Rocketmq cluster.
 */
@OverrideImplementor(ProfileSnapshotSender.class)
public class RocketmqProfileSnapshotSender extends ProfileSnapshotSender implements RocketmqConnectionStatusListener  {
    private static final ILog LOGGER = LogManager.getLogger(RocketmqProfileSnapshotSender.class);

    private String topic;

    private DefaultMQProducer producer;

    @Override
    public void prepare() {
        RocketmqProducerManager producerManager = ServiceManager.INSTANCE.findService(RocketmqProducerManager.class);
        producerManager.addListener(this);
        topic = producerManager.formatTopicNameThenRegister(RocketmqReporterPluginConfig.Plugin.Rocketmq.TOPIC_PROFILING);
    }

    @Override
    public void boot() {
    }

    @Override
    public void send(final List<TracingThreadSnapshot> buffer) {
        if (producer == null) {
            return;
        }
        for (TracingThreadSnapshot snapshot : buffer) {
            final ThreadSnapshot object = snapshot.transform();
            if (LOGGER.isDebugEnable()) {
                LOGGER.debug("Thread snapshot reporting, topic: {}, taskId: {}, sequence:{}, traceId: {}",
                        object.getTaskId(), object.getSequence(), object.getTraceSegmentId()
                );
            }
            try {
                producer.send(new Message(
                        topic,
                        object.toByteArray()),
                        RocketmqReporterPluginConfig.Plugin.Rocketmq.PRODUCE_TIMEOUT
                );
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("Failed to report TracingThreadSnapshot.", e);
            }
        }
    }

    @Override
    public void onStatusChanged(RocketmqConnectionStatus status) {
        if (status == RocketmqConnectionStatus.CONNECTED) {
            producer = ServiceManager.INSTANCE.findService(RocketmqProducerManager.class).getProducer();
        }
    }
}
