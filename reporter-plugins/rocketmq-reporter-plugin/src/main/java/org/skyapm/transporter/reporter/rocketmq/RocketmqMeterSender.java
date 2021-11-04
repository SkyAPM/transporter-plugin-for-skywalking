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
import org.apache.skywalking.apm.agent.core.meter.BaseMeter;
import org.apache.skywalking.apm.agent.core.meter.MeterId;
import org.apache.skywalking.apm.agent.core.meter.MeterSender;
import org.apache.skywalking.apm.agent.core.meter.MeterService;
import org.apache.skywalking.apm.network.language.agent.v3.MeterDataCollection;

import java.util.Map;

/**
 * A report to send Metrics data of meter system to Rocketmq cluster.
 */
@OverrideImplementor(MeterSender.class)
public class RocketmqMeterSender extends MeterSender implements RocketmqConnectionStatusListener {
    private static final ILog LOGGER = LogManager.getLogger(RocketmqTraceSegmentServiceClient.class);

    private String topic;
    private DefaultMQProducer producer;

    @Override
    public void prepare() {
        RocketmqProducerManager producerManager = ServiceManager.INSTANCE.findService(RocketmqProducerManager.class);
        producerManager.addListener(this);
        topic = producerManager.formatTopicNameThenRegister(RocketmqReporterPluginConfig.Plugin.Rocketmq.TOPIC_METER);
    }

    @Override
    public void boot() {
    }

    @Override
    public void send(Map<MeterId, BaseMeter> meterMap, MeterService meterService) {
        if (producer == null) {
            return;
        }
        MeterDataCollection.Builder builder = MeterDataCollection.newBuilder();
        transform(meterMap, meterData -> {
            if (LOGGER.isDebugEnable()) {
                LOGGER.debug("Meter data reporting, instance: {}", meterData.getServiceInstance());
            }
            builder.addMeterData(meterData);
        });
        try {
            producer.send(new Message(topic, builder.build().toByteArray()),
                    RocketmqReporterPluginConfig.Plugin.Rocketmq.PRODUCE_TIMEOUT);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Failed to report Meter.", e);
        }
    }

    @Override
    public void onStatusChanged(RocketmqConnectionStatus status) {
        if (status == RocketmqConnectionStatus.CONNECTED) {
            producer = ServiceManager.INSTANCE.findService(RocketmqProducerManager.class).getProducer();
        }
    }
}
