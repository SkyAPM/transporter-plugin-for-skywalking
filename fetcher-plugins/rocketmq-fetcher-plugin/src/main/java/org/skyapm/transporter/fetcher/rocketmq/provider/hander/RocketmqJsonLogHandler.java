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

package org.skyapm.transporter.fetcher.rocketmq.provider.hander;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.skywalking.apm.network.logging.v3.LogData;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.util.ProtoBufJsonUtils;
import org.skyapm.transporter.fetcher.rocketmq.module.RocketmqFetcherConfig;

import java.io.IOException;

@Slf4j
public class RocketmqJsonLogHandler extends RocketmqLogHandler {
    private final RocketmqFetcherConfig config;

    public RocketmqJsonLogHandler(ModuleManager moduleManager, RocketmqFetcherConfig config) {
        super(moduleManager, config);
        this.config = config;
    }

    @Override
    public String getTopic() {
        return config.getTopicNameOfJsonLogs();
    }

    @Override
    protected String getDataFormat() {
        return "json";
    }

    @Override
    protected LogData parseConsumerRecord(MessageExt messageExt) throws IOException {
        LogData.Builder logDataBuilder = LogData.newBuilder();
        ProtoBufJsonUtils.fromJSON(messageExt.getBody().toString(), logDataBuilder);
        return logDataBuilder.build();
    }
}
