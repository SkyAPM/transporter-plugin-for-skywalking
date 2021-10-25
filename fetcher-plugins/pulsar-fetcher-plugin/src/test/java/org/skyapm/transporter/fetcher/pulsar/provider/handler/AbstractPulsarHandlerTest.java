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

package org.skyapm.transporter.fetcher.pulsar.provider.handler;

import org.apache.pulsar.client.api.Message;
import org.junit.Test;
import org.skyapm.transporter.fetcher.pulsar.module.PulsarFetcherConfig;

import static org.junit.Assert.assertEquals;

public class AbstractPulsarHandlerTest {
    @Test
    public void testGetTopic() {
        PulsarFetcherConfig config = new PulsarFetcherConfig();

        String plainTopic = config.getTopicNameOfTracingSegments();

        MockPulsarHandler pulsarHandler = new MockPulsarHandler(plainTopic, config);

        String expectValue = config.getDomain() + "://"
            + config.getNamespace() + "/"
            + plainTopic;
        assertEquals(pulsarHandler.getTopic(), expectValue);

        String namespace = "tenant/skywalking";
        config.setNamespace(namespace);
        expectValue = config.getDomain() + "://"
            + config.getNamespace() + "/"
            + plainTopic;
        assertEquals(pulsarHandler.getTopic(), expectValue);

    }

    static class MockPulsarHandler extends AbstractPulsarHandler {
        private String plainTopic;

        public MockPulsarHandler(String plainTopic, PulsarFetcherConfig config) {
            super(config);
            this.plainTopic = plainTopic;
        }

        @Override
        protected String getPlainTopic() {
            return plainTopic;
        }

        @Override
        public void handle(Message<byte[]> message) {

        }
    }

}