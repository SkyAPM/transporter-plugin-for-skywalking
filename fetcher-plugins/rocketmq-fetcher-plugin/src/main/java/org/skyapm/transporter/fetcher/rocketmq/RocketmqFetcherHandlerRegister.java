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

package org.skyapm.transporter.fetcher.rocketmq;

import io.netty.channel.DefaultChannelId;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.server.pool.CustomThreadFactory;
import org.skyapm.transporter.fetcher.rocketmq.module.RocketmqFetcherConfig;
import org.skyapm.transporter.fetcher.rocketmq.provider.hander.RocketmqHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Configuring and initializing a Rocketmq Consumer client as a dispatcher to delivery  Message to registered handler by topic.
 */
@Slf4j
public class RocketmqFetcherHandlerRegister implements Runnable {

    private Map<String, RocketmqHandler> handlerMap = new ConcurrentHashMap<>();
    private DefaultLitePullConsumer consumer;
    private final RocketmqFetcherConfig config;
    private int threadPoolSize = Runtime.getRuntime().availableProcessors() * 2;
    private int threadPoolQueueSize = 10000;
    private final ThreadPoolExecutor executor;
    private DefaultMQAdminExt defaultMQAdminExt;

    public RocketmqFetcherHandlerRegister(RocketmqFetcherConfig config) {
        this.config = config;

        if (config.getRocketmqHandlerThreadPoolSize() > 0) {
            threadPoolSize = config.getRocketmqHandlerThreadPoolSize();
        }
        if (config.getRocketmqHandlerThreadPoolQueueSize() > 0) {
            threadPoolQueueSize = config.getRocketmqHandlerThreadPoolQueueSize();
        }

        consumer = new DefaultLitePullConsumer(config.getConsumerGroup());
        consumer.setNamesrvAddr(config.getNameServers());

        executor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize,
                60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(threadPoolQueueSize),
                new CustomThreadFactory("RocketmqConsumer"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    public void register(RocketmqHandler handler) {
        handlerMap.put(handler.getTopic(), handler);
    }

    public void start() throws ModuleStartException {

        createTopicIfNeeded(handlerMap.keySet());
        handlerMap.keySet().forEach(topic -> {
            try {
                consumer.subscribe(topic, "*");
            } catch (MQClientException e) {
                log.error("skywalking consumer subscribe topic {} fail", topic);
                e.printStackTrace();
            }
        });
        consumer.setPullBatchSize(config.getPullBatchSize());
        consumer.setPollTimeoutMillis(config.getPollTimeoutMillis());

        try {
            consumer.start();
        } catch (MQClientException e) {
            log.error("rocketmq consumer start failed");
            e.printStackTrace();
        }
        executor.submit(this);
    }

    @Override
    public void run() {
        while (true) {
            try {
                List<MessageExt> msgs = consumer.poll();
                if (msgs.isEmpty()) {
                    continue;
                }
                msgs.forEach(msg -> handlerMap.get(msg.getTopic()).handle(msg));
                consumer.commitSync();
            } catch (Exception e) {
                log.error("rocketmq handle message error", e);
                e.printStackTrace();
            }
        }
    }

    private void createTopicIfNeeded(Collection<String> topics) throws ModuleStartException {
        defaultMQAdminExt = new DefaultMQAdminExt();
        defaultMQAdminExt.setNamesrvAddr(config.getNameServers());
        DefaultChannelId.newInstance();
        try {
            defaultMQAdminExt.start();
        } catch (MQClientException e) {
            log.error("Rocketmq admin create error", e);
        }

        Set<String> missedTopics = new HashSet<>();
        try {
            Set<String> topicList = defaultMQAdminExt.fetchAllTopicList().getTopicList();
            for (String topic : topics) {
                if (!topicList.contains(topic)) {
                    missedTopics.add(topic);
                }
            }
        } catch (RemotingException | MQClientException | InterruptedException e) {
            log.error("Get Rocketmq topics error", e);
            defaultMQAdminExt.shutdown();
        }

        if (!missedTopics.isEmpty()) {
            log.info("Topics" + missedTopics.toString() + " not exist.");
            for (String topic : missedTopics) {
                try {
                    defaultMQAdminExt.createTopic(config.getCluster(), topic, config.getQueues());
                } catch (MQClientException e) {
                    throw new ModuleStartException("Failed to create Rocketmq Topic" + topic + ".", e);
                }
            }
        }
    }
}
