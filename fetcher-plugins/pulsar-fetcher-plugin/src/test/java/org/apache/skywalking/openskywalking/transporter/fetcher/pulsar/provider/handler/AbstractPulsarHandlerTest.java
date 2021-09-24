package org.apache.skywalking.openskywalking.transporter.fetcher.pulsar.provider.handler;

import org.apache.pulsar.client.api.Message;
import org.junit.Test;
import org.apache.skywalking.openskywalking.transporter.fetcher.pulsar.module.PulsarFetcherConfig;

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