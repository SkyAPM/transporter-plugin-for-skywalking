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

package org.apache.skywalking.openskywalking.transporter.fetcher.pulsar.provider.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Message;
import org.apache.skywalking.apm.network.logging.v3.LogData;
import org.apache.skywalking.openskywalking.transporter.fetcher.pulsar.module.PulsarFetcherConfig;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.util.ProtoBufJsonUtils;

import java.io.IOException;

@Slf4j
public class JsonLogHandler extends LogHandler {

    public JsonLogHandler(ModuleManager moduleManager, PulsarFetcherConfig config) {
        super(moduleManager, config);
    }

    @Override
    protected String getDataFormat() {
        return "json";
    }

    @Override
    protected LogData parseConsumerRecord(Message<byte[]> message) throws IOException {
        LogData.Builder logDataBuilder = LogData.newBuilder();
        ProtoBufJsonUtils.fromJSON(new String(message.getValue()), logDataBuilder);
        return logDataBuilder.build();
    }

    @Override
    protected String getPlainTopic() {
        return config.getTopicNameOfJsonLogs();
    }
}
