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

package org.skyapm.transporter.reporter.rocketmq;

import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class RocketmqProducerManagerTest {
    @Test
    public void testAddListener() throws Exception {
        RocketmqProducerManager rocketmqProducerManager = new RocketmqProducerManager();
        AtomicInteger counter = new AtomicInteger();
        int times = 100;
        for (int i = 0; i < times; i++) {
            rocketmqProducerManager.addListener(new MockListener(counter));
        }
        Whitebox.invokeMethod(rocketmqProducerManager, "notifyListeners", RocketmqConnectionStatus.CONNECTED);
    }

    @Test
    public void testFormatTopicNameThenRegister() {
        RocketmqProducerManager rocketmqProducerManager = new RocketmqProducerManager();
        RocketmqReporterPluginConfig.Plugin.Rocketmq.NAMESPACE = "producer";
        String value = rocketmqProducerManager.formatTopicNameThenRegister(RocketmqReporterPluginConfig.Plugin.Rocketmq.TOPIC_METRICS);
        String expectValue = RocketmqReporterPluginConfig.Plugin.Rocketmq.NAMESPACE + "-" + RocketmqReporterPluginConfig.Plugin.Rocketmq.TOPIC_METRICS;
        assertEquals(value, expectValue);

        RocketmqReporterPluginConfig.Plugin.Rocketmq.NAMESPACE = "";
        value = rocketmqProducerManager.formatTopicNameThenRegister(RocketmqReporterPluginConfig.Plugin.Rocketmq.TOPIC_METRICS);
        assertEquals(RocketmqReporterPluginConfig.Plugin.Rocketmq.TOPIC_METRICS, value);
    }

    static class MockListener implements RocketmqConnectionStatusListener {

        private AtomicInteger counter;

        public MockListener(AtomicInteger counter) {
            this.counter = counter;
        }

        @Override
        public void onStatusChanged(RocketmqConnectionStatus status) {
            counter.incrementAndGet();
        }
    }
}
