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

package org.skyapm.transporter.fetcher.pulsar.mock;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.common.api.EncryptionContext;
import org.apache.skywalking.apm.util.StringUtil;

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
