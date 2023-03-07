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

import org.apache.skywalking.oap.server.library.util.StringUtil;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.skyapm.transporter.fetcher.rocketmq.module.RocketmqFetcherConfig;

public abstract class AbstractRocketmqHandler implements RocketmqHandler {
    protected RocketmqFetcherConfig config;

    public AbstractRocketmqHandler(ModuleManager manager, RocketmqFetcherConfig config) {
        this.config = config;
    }

    @Override
    public String getTopic() {
        StringBuilder sb = new StringBuilder();

        if (StringUtil.isNotBlank(config.getNamespace())) {
            sb.append(config.getNamespace()).append("-");
        }
        sb.append(getPlainTopic());
        return sb.toString();
    }

    protected abstract String getPlainTopic();
}
