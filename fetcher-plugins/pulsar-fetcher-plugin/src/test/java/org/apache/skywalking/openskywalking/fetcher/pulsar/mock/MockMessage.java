package org.apache.skywalking.openskywalking.fetcher.pulsar.mock;

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.common.api.EncryptionContext;
import org.apache.skywalking.apm.util.StringUtil;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class MockMessage implements Message {
    private byte[] data;
    private String topic;
    private String key;

    public MockMessage(String topic, byte[] data, String key) {
        this.data = data;
        this.topic = topic;
        this.key = key;
    }

    @Override
    public Map<String, String> getProperties() {
        return null;
    }

    @Override
    public boolean hasProperty(String name) {
        return false;
    }

    @Override
    public String getProperty(String name) {
        return null;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Object getValue() {
        return data;
    }

    @Override
    public MessageId getMessageId() {
        return null;
    }

    @Override
    public long getPublishTime() {
        return 0;
    }

    @Override
    public long getEventTime() {
        return 0;
    }

    @Override
    public long getSequenceId() {
        return 0;
    }

    @Override
    public String getProducerName() {
        return null;
    }

    @Override
    public boolean hasKey() {
        return StringUtil.isNotBlank(key);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean hasBase64EncodedKey() {
        return false;
    }

    @Override
    public byte[] getKeyBytes() {
        return key.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean hasOrderingKey() {
        return false;
    }

    @Override
    public byte[] getOrderingKey() {
        return new byte[0];
    }

    @Override
    public String getTopicName() {
        return topic;
    }

    @Override
    public Optional<EncryptionContext> getEncryptionCtx() {
        return Optional.empty();
    }

    @Override
    public int getRedeliveryCount() {
        return 0;
    }

    @Override
    public byte[] getSchemaVersion() {
        return new byte[0];
    }

    @Override
    public boolean isReplicated() {
        return false;
    }

    @Override
    public String getReplicatedFrom() {
        return null;
    }

    @Override
    public void release() {

    }
}
