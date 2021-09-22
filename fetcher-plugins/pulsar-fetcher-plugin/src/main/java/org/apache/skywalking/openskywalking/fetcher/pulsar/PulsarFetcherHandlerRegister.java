/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.skywalking.openskywalking.fetcher.pulsar;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.MessageListener;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.skywalking.openskywalking.fetcher.pulsar.module.PulsarFetcherConfig;
import org.apache.skywalking.openskywalking.fetcher.pulsar.provider.handler.PulsarHandler;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.server.pool.CustomThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PulsarFetcherHandlerRegister {
    private ImmutableMap.Builder<String, PulsarHandler> builder = ImmutableMap.builder();
    private ImmutableMap<String, PulsarHandler> handlerMap;
    private Consumer<byte[]> consumer = null;
    private PulsarClient client = null;
    private final PulsarFetcherConfig config;
    private int threadPoolSize = Runtime.getRuntime().availableProcessors() * 2;
    private int threadPoolQueueSize = 10000;
    private final ThreadPoolExecutor executor;

    public PulsarFetcherHandlerRegister(PulsarFetcherConfig config) throws ModuleStartException {
        this.config = config;

        try {
            client = PulsarClient.builder()
                                 .serviceUrl(config.getBootstrapServers())
                                 .loadConf(config.getPulsarClientConfig())
                                 .build();
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
        if (config.getPulsarHandlerThreadPoolSize() > 0) {
            threadPoolSize = config.getPulsarHandlerThreadPoolSize();
        }
        if (config.getPulsarHandlerThreadPoolQueueSize() > 0) {
            threadPoolQueueSize = config.getPulsarHandlerThreadPoolQueueSize();
        }
        executor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize,
                                          60, TimeUnit.SECONDS,
                                          new ArrayBlockingQueue(threadPoolQueueSize),
                                          new CustomThreadFactory("PulsarConsumer"),
                                          new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    public void register(PulsarHandler handler) {
        builder.put(handler.getTopic(), handler);
    }

    public void start() throws ModuleStartException {
        handlerMap = builder.build();
        try {
            consumer = client.newConsumer()
                             .loadConf(config.getPulsarConsumerConfig())
                             .topics(handlerMap.keySet().asList())
                             .subscriptionName(config.getSubscription())
                             .messageListener((MessageListener<byte[]>) (consumer, msg) -> {
                                 try {
                                     executor.submit(() -> handlerMap.get(msg.getTopicName()).handle(msg));
                                     consumer.acknowledge(msg);
                                 } catch (Exception e) {
                                     consumer.negativeAcknowledge(msg);
                                     log.error("Pulsar handle message error.", e);
                                 }
                             })
                             .subscribe();
        } catch (PulsarClientException e) {
            throw new ModuleStartException("create pulsar consumer failed.", e);
        }
    }

}
