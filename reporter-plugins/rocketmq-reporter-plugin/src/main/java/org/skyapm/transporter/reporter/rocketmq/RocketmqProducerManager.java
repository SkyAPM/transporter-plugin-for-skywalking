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

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.skywalking.apm.agent.core.boot.BootService;
import org.apache.skywalking.apm.agent.core.boot.DefaultImplementor;
import org.apache.skywalking.apm.agent.core.boot.DefaultNamedThreadFactory;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.remote.GRPCChannelManager;
import org.apache.skywalking.apm.util.RunnableWithExceptionProtection;
import org.apache.skywalking.apm.util.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Configuring, initializing and holding a RocketmqProducer instance for reporters.
 */
@DefaultImplementor
public class RocketmqProducerManager implements BootService, Runnable {

    private static final ILog LOGGER = LogManager.getLogger(RocketmqProducerManager.class);

    private Set<String> topics = new HashSet<>();
    private List<RocketmqConnectionStatusListener> listeners = new ArrayList<>();

    private volatile DefaultMQProducer producer;

    private ScheduledFuture<?> bootProducerFuture;

    private DefaultMQAdminExt defaultMQAdminExt;

    @Override
    public void prepare() {
    }

    @Override
    public void boot() {
        bootProducerFuture = Executors.newSingleThreadScheduledExecutor(
                new DefaultNamedThreadFactory("rocketmqProducerInitThread")
        ).scheduleAtFixedRate(new RunnableWithExceptionProtection(
                this,
                t -> LOGGER.error("unexpected exception.", t)
        ), 0, 120, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        // check topic
        if (!checkTopicsExist()) {
            return;
        }

        try {
            producer = new DefaultMQProducer(RocketmqReporterPluginConfig.Plugin.Rocketmq.PRODUCE_GROUP);
            producer.setNamesrvAddr(RocketmqReporterPluginConfig.Plugin.Rocketmq.NAME_SERVERS);
            producer.setVipChannelEnabled(RocketmqReporterPluginConfig.Plugin.Rocketmq.VIP_ENABLE);
            producer.setMaxMessageSize(RocketmqReporterPluginConfig.Plugin.Rocketmq.MAX_MESSAGE_SIZE);
            producer.setRetryTimesWhenSendFailed(RocketmqReporterPluginConfig.Plugin.Rocketmq.RETRY_TIMES);
            producer.setRetryAnotherBrokerWhenNotStoreOK(RocketmqReporterPluginConfig.Plugin.Rocketmq.RETRY_ANOTHER_BROKER);
            producer.start();
        } catch (Exception e) {
            LOGGER.error(e, "Rocketmq producer initialization failed. nameserver: {}", RocketmqReporterPluginConfig.Plugin.Rocketmq.NAME_SERVERS);
            return;
        }

        //notify listeners to send data if no exception been throw
        notifyListeners(RocketmqConnectionStatus.CONNECTED);
        bootProducerFuture.cancel(true);
    }

    String formatTopicNameThenRegister(String topic) {
        String topicName = StringUtil.isBlank(RocketmqReporterPluginConfig.Plugin.Rocketmq.NAMESPACE) ? topic
                : RocketmqReporterPluginConfig.Plugin.Rocketmq.NAMESPACE + "-" + topic;
        topics.add(topicName);
        return topicName;
    }

    public void addListener(RocketmqConnectionStatusListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public final DefaultMQProducer getProducer() {
        return producer;
    }

    @Override
    public void onComplete() {
    }

    @Override
    public int priority() {
        return ServiceManager.INSTANCE.findService(GRPCChannelManager.class).priority() - 1;
    }

    @Override
    public void shutdown() throws Throwable {
        producer.shutdown();
    }

    private boolean checkTopicsExist() {
        // create rocketmq admin client
        defaultMQAdminExt = new DefaultMQAdminExt();
        defaultMQAdminExt.setNamesrvAddr(RocketmqReporterPluginConfig.Plugin.Rocketmq.NAME_SERVERS);
        try {
            defaultMQAdminExt.start();
        } catch (MQClientException e) {
            LOGGER.error("Rocketmq admin create error", e);
            return false;
        }

        try {
            Set<String> topicList = defaultMQAdminExt.fetchAllTopicList().getTopicList();
            for (String topic : topics) {
                if (!topicList.contains(topic)) {
                    LOGGER.warn("Rocketmq topic {} is not exist, connect to name_server abort", topic);
                    return false;
                }
            }
        } catch (RemotingException | MQClientException | InterruptedException e) {
            LOGGER.error(e, "Get rocketmq topics error , {}", e.getCause());
            return false;
        } finally {
            defaultMQAdminExt.shutdown();
        }
        return true;
    }

    private void notifyListeners(RocketmqConnectionStatus status) {
        for (RocketmqConnectionStatusListener listener : listeners) {
            listener.onStatusChanged(status);
        }
    }
}
