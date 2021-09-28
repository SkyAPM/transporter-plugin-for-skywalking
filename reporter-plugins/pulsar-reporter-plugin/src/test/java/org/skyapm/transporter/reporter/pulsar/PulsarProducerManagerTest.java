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

package org.skyapm.transporter.reporter.pulsar;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.pulsar.common.naming.TopicDomain;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;

public class PulsarProducerManagerTest {

    @Test
    public void testFormatTopicNameThenRegister() {
        PulsarProducerManager pulsarProducerManager = new PulsarProducerManager();
        PulsarReporterPluginConfig.Plugin.Pulsar.NAMESPACE = "public/default";
        PulsarReporterPluginConfig.Plugin.Pulsar.DOMAIN = TopicDomain.persistent.value();
        String value = pulsarProducerManager.formatTopicNameThenRegister(
            PulsarReporterPluginConfig.Plugin.Pulsar.TOPIC_METRICS);
        String expectValue = PulsarReporterPluginConfig.Plugin.Pulsar.DOMAIN + "://"
            + PulsarReporterPluginConfig.Plugin.Pulsar.NAMESPACE + "/"
            + PulsarReporterPluginConfig.Plugin.Pulsar.TOPIC_METRICS;
        assertEquals(value, expectValue);
    }

    @Test
    public void testAddListener() throws Exception {
        PulsarProducerManager pulsarProducerManager = new PulsarProducerManager();
        AtomicInteger counter = new AtomicInteger();
        int times = 100;
        for (int i = 0; i < times; i++) {
            pulsarProducerManager.addListener(new MockListener(counter));
        }
        Whitebox.invokeMethod(pulsarProducerManager, "notifyListeners", PulsarConnectionStatus.CONNECTED);

        assertEquals(counter.get(), times);
    }

    static class MockListener implements PulsarConnectionStatusListener {

        private AtomicInteger counter;

        public MockListener(AtomicInteger counter) {
            this.counter = counter;
        }

        @Override
        public void onStatusChanged(PulsarConnectionStatus status) {
            counter.incrementAndGet();
        }
    }

}