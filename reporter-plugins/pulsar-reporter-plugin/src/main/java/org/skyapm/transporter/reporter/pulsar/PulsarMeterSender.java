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

import java.util.Map;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.skywalking.apm.agent.core.boot.OverrideImplementor;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import org.apache.skywalking.apm.agent.core.conf.Config;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.meter.BaseMeter;
import org.apache.skywalking.apm.agent.core.meter.MeterId;
import org.apache.skywalking.apm.agent.core.meter.MeterSender;
import org.apache.skywalking.apm.agent.core.meter.MeterService;
import org.apache.skywalking.apm.network.language.agent.v3.MeterDataCollection;

/**
 * A report to send Metrics data of meter system to Kafka Broker.
 */
@OverrideImplementor(MeterSender.class)
public class PulsarMeterSender extends MeterSender implements PulsarConnectionStatusListener {
    private static final ILog LOGGER = LogManager.getLogger(PulsarMeterSender.class);

    private String topic;
    private Producer<byte[]> producer;

    @Override
    public void prepare() {
        PulsarProducerManager producerManager = ServiceManager.INSTANCE.findService(PulsarProducerManager.class);
        producerManager.addListener(this);
        topic = producerManager.formatTopicNameThenRegister(PulsarReporterPluginConfig.Plugin.Pulsar.TOPIC_METER);
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
            producer.newMessage()
                    .key(Config.Agent.INSTANCE_NAME)
                    .value(builder.build().toByteArray())
                    .send();
            producer.flush();
        } catch (PulsarClientException e) {
            LOGGER.error(e, "Send Meter data failed.");
        }
    }

    @Override
    public void onStatusChanged(PulsarConnectionStatus status) {
        if (status == PulsarConnectionStatus.CONNECTED) {
            producer = ServiceManager.INSTANCE.findService(PulsarProducerManager.class).getProducer(topic);
        }
    }
}
