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

package org.skyapm.transporter.fetcher.rocketmq.provider;

import org.apache.skywalking.oap.server.library.module.ModuleConfig;
import org.skyapm.transporter.fetcher.rocketmq.module.RocketmqFetcherConfig;
import org.skyapm.transporter.fetcher.rocketmq.module.RocketmqFetcherModule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RocketmqFetcherProviderTest {
    RocketmqFetcherProvider provider = new RocketmqFetcherProvider();

    @Test
    public void name() {
        assertEquals("default", provider.name());
    }

    @Test
    public void module() {
        assertEquals(RocketmqFetcherModule.class, provider.module());
    }

    @Test
    public void createConfigBeanIfAbsent() {
        ModuleConfig moduleConfig = provider.createConfigBeanIfAbsent();
        assertTrue(moduleConfig instanceof RocketmqFetcherConfig);
    }

}
