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
import org.apache.skywalking.apm.agent.core.boot.BootService;
import org.apache.skywalking.apm.agent.core.boot.DefaultNamedThreadFactory;
import org.apache.skywalking.apm.agent.core.boot.OverrideImplementor;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import org.apache.skywalking.apm.agent.core.conf.Config;
import org.apache.skywalking.apm.agent.core.jvm.LoadedLibraryCollector;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.os.OSUtil;
import org.apache.skywalking.apm.agent.core.remote.ServiceManagementClient;
import org.apache.skywalking.apm.network.common.v3.KeyStringValuePair;
import org.apache.skywalking.apm.network.management.v3.InstancePingPkg;
import org.apache.skywalking.apm.network.management.v3.InstanceProperties;
import org.apache.skywalking.apm.util.RunnableWithExceptionProtection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A service management data(Instance registering properties and Instance pinging) reporter.
 */
@OverrideImplementor(ServiceManagementClient.class)
public class RocketmqServiceManagementServiceClient implements BootService, Runnable, RocketmqConnectionStatusListener  {
    private static final ILog LOGGER = LogManager.getLogger(RocketmqServiceManagementServiceClient.class);

    private static List<KeyStringValuePair> SERVICE_INSTANCE_PROPERTIES;

    private static final String TOPIC_TAG_REGISTER = "register-";
    private static final String TOPIC_TAG_PING = "ping-";

    private ScheduledFuture<?> heartbeatFuture;
    private DefaultMQProducer producer;

    private String topic;
    private AtomicInteger sendPropertiesCounter = new AtomicInteger(0);

    @Override
    public void prepare() {
        RocketmqProducerManager producerManager = ServiceManager.INSTANCE.findService(RocketmqProducerManager.class);
        producerManager.addListener(this);
        topic = producerManager.formatTopicNameThenRegister(RocketmqReporterPluginConfig.Plugin.Rocketmq.TOPIC_MANAGEMENT);

        SERVICE_INSTANCE_PROPERTIES = new ArrayList<>();
        for (String key : Config.Agent.INSTANCE_PROPERTIES.keySet()) {
            SERVICE_INSTANCE_PROPERTIES.add(KeyStringValuePair.newBuilder()
                    .setKey(key)
                    .setValue(Config.Agent.INSTANCE_PROPERTIES.get(key))
                    .build());
        }
    }

    @Override
    public void boot() {
        heartbeatFuture = Executors.newSingleThreadScheduledExecutor(
                new DefaultNamedThreadFactory("ServiceManagementClientRocketmqProducer")
        ).scheduleAtFixedRate(new RunnableWithExceptionProtection(
                this,
                t -> LOGGER.error("unexpected exception.", t)
        ), 0, Config.Collector.HEARTBEAT_PERIOD, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        if (producer == null) {
            return;
        }
        InstanceProperties instance = InstanceProperties.newBuilder()
                .setService(Config.Agent.SERVICE_NAME)
                .setServiceInstance(Config.Agent.INSTANCE_NAME)
                .addAllProperties(OSUtil.buildOSInfo(
                        Config.OsInfo.IPV4_LIST_SIZE))
                .addAllProperties(SERVICE_INSTANCE_PROPERTIES)
                .addAllProperties(LoadedLibraryCollector.buildJVMInfo())
                .build();
        if (Math.abs(sendPropertiesCounter.getAndAdd(1)) % Config.Collector.PROPERTIES_REPORT_PERIOD_FACTOR == 0) {
            Message message = new Message(topic, instance.toByteArray());
            message.setTags(TOPIC_TAG_REGISTER + instance.getServiceInstance());
            try {
                producer.send(message,
                        RocketmqReporterPluginConfig.Plugin.Rocketmq.PRODUCE_TIMEOUT);
            } catch (Exception e) {
                LOGGER.error("Failed to report Management.", e);
            }
        } else {
            InstancePingPkg ping = InstancePingPkg.newBuilder()
                    .setService(Config.Agent.SERVICE_NAME)
                    .setServiceInstance(Config.Agent.INSTANCE_NAME)
                    .build();
            if (LOGGER.isDebugEnable()) {
                LOGGER.debug("Heartbeat reporting, instance: {}", ping.getServiceInstance());
            }
            Message message = new Message(topic, ping.toByteArray());
            message.setTags(TOPIC_TAG_PING + instance.getServiceInstance());
            try {
                producer.send(message,
                        RocketmqReporterPluginConfig.Plugin.Rocketmq.PRODUCE_TIMEOUT);
            } catch (Exception e) {
                LOGGER.error("Failed to report Heartbeat.", e);
            }
        }
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onStatusChanged(RocketmqConnectionStatus status) {
        if (status == RocketmqConnectionStatus.CONNECTED) {
            producer = ServiceManager.INSTANCE.findService(RocketmqProducerManager.class).getProducer();
        }
    }

    @Override
    public void shutdown() {
        heartbeatFuture.cancel(true);
    }

}
