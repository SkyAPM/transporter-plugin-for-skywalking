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

package org.skyapm.transporter.fetcher.pulsar;

import com.google.common.collect.ImmutableMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.MessageListener;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.server.pool.CustomThreadFactory;
import org.skyapm.transporter.fetcher.pulsar.module.PulsarFetcherConfig;
import org.skyapm.transporter.fetcher.pulsar.provider.handler.PulsarHandler;

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

    public PulsarFetcherHandlerRegister(PulsarFetcherConfig config) {
        this.config = config;

        try {
            client = PulsarClient.builder()
                                 .loadConf(config.getPulsarClientConfig())
                                 .build();
        } catch (PulsarClientException e) {
           log.error("pulsar client build failed", e);
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
                             .messageListener((MessageListener<byte[]>) (consumer, msg) -> {
                                 log.info("Get data from pulsar, begin to handle.");
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
