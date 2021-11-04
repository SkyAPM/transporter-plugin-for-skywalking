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

package org.skyapm.transporter.fetcher.rocketmq.provider.handler;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.skyapm.transporter.fetcher.rocketmq.mock.MockModuleManager;
import org.skyapm.transporter.fetcher.rocketmq.module.RocketmqFetcherConfig;
import org.skyapm.transporter.fetcher.rocketmq.provider.hander.AbstractRocketmqHandler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractRocketmqHandlerTest {
    @Test
    public void testGetTopic() {
        RocketmqFetcherConfig config = new RocketmqFetcherConfig();

        MockModuleManager manager = new MockModuleManager() {
            @Override
            protected void init() {
            }
        };
        String plainTopic = config.getTopicNameOfTracingSegments();

        MockRocketmqHandler kafkaHandler = new MockRocketmqHandler(plainTopic, manager, config);

        //  unset namespace
        assertEquals(kafkaHandler.getTopic(), plainTopic);

        //set namespace only
        String namespace = "product";
        config.setNamespace(namespace);
        assertEquals(namespace + "-" + plainTopic, kafkaHandler.getTopic());

        //set namespace
        config.setNamespace(namespace);
    }

    static class MockRocketmqHandler extends AbstractRocketmqHandler {
        private String plainTopic;

        public MockRocketmqHandler(String plainTopic, ModuleManager manager, RocketmqFetcherConfig config) {
            super(manager, config);
            this.plainTopic = plainTopic;
        }

        @Override
        protected String getPlainTopic() {
            return plainTopic;
        }

        @Override
        public void handle(MessageExt record) {

        }
    }
}
