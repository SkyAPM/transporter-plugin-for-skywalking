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

package org.skyapm.transporter.fetcher.rocketmq.provider.handler;

import com.google.common.collect.Lists;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.skywalking.apm.network.common.v3.CPU;
import org.apache.skywalking.apm.network.language.agent.v3.GC;
import org.apache.skywalking.apm.network.language.agent.v3.JVMMetric;
import org.apache.skywalking.apm.network.language.agent.v3.JVMMetricCollection;
import org.apache.skywalking.apm.network.language.agent.v3.Memory;
import org.apache.skywalking.apm.network.language.agent.v3.MemoryPool;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.config.NamingControl;
import org.apache.skywalking.oap.server.core.config.group.EndpointNameGrouping;
import org.apache.skywalking.oap.server.core.source.ISource;
import org.apache.skywalking.oap.server.core.source.ServiceInstanceJVMCPU;
import org.apache.skywalking.oap.server.core.source.ServiceInstanceJVMGC;
import org.apache.skywalking.oap.server.core.source.ServiceInstanceJVMMemory;
import org.apache.skywalking.oap.server.core.source.ServiceInstanceJVMMemoryPool;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;
import org.apache.skywalking.oap.server.telemetry.api.MetricsCreator;
import org.apache.skywalking.oap.server.telemetry.none.MetricsCreatorNoop;
import org.skyapm.transporter.fetcher.rocketmq.mock.MockModuleManager;
import org.skyapm.transporter.fetcher.rocketmq.mock.MockModuleProvider;
import org.skyapm.transporter.fetcher.rocketmq.module.RocketmqFetcherConfig;
import org.skyapm.transporter.fetcher.rocketmq.provider.hander.JVMMetricsHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;

public class JVMMetricsHandlerTest {
    private static final String TOPIC_NAME = "skywalking-metrics";
    private JVMMetricsHandler handler = null;
    private RocketmqFetcherConfig config = new RocketmqFetcherConfig();

    private ModuleManager manager;

    @ClassRule
    public static SourceReceiverRule SOURCE_RECEIVER = new SourceReceiverRule() {

        @Override
        protected void verify(final List<ISource> sourceList) throws Throwable {
            Assert.assertTrue(sourceList.get(0) instanceof ServiceInstanceJVMCPU);
            ServiceInstanceJVMCPU serviceInstanceJVMCPU = (ServiceInstanceJVMCPU) sourceList.get(0);
            Assert.assertThat(serviceInstanceJVMCPU.getUsePercent(), is(1.0));
            Assert.assertTrue(sourceList.get(1) instanceof ServiceInstanceJVMMemory);
            Assert.assertTrue(sourceList.get(2) instanceof ServiceInstanceJVMMemoryPool);
            Assert.assertTrue(sourceList.get(3) instanceof ServiceInstanceJVMGC);
        }
    };

    @Before
    public void setup() {
        manager = new MockModuleManager() {
            @Override
            protected void init() {
                register(CoreModule.NAME, () -> new MockModuleProvider() {
                    @Override
                    protected void register() {
                        registerServiceImplementation(NamingControl.class, new NamingControl(
                                512, 512, 512, new EndpointNameGrouping()));
                        registerServiceImplementation(SourceReceiver.class, SOURCE_RECEIVER);
                    }
                });
                register(TelemetryModule.NAME, () -> new MockModuleProvider() {
                    @Override
                    protected void register() {
                        registerServiceImplementation(MetricsCreator.class, new MetricsCreatorNoop());
                    }
                });
            }
        };
        handler = new JVMMetricsHandler(manager, config);
    }

    @Test
    public void testTopicName() {
        Assert.assertEquals(handler.getTopic(), TOPIC_NAME);
    }

    @Test
    public void testHandler() {
        long currentTimeMillis = System.currentTimeMillis();

        JVMMetric.Builder jvmBuilder = JVMMetric.newBuilder();
        jvmBuilder.setTime(currentTimeMillis);
        jvmBuilder.setCpu(CPU.newBuilder().setUsagePercent(0.98d).build());
        jvmBuilder.addAllMemory(Lists.newArrayList(Memory.newBuilder().setInit(10).setUsed(100).setIsHeap(false).build()));
        jvmBuilder.addAllMemoryPool(Lists.newArrayList(MemoryPool.newBuilder().build()));
        jvmBuilder.addAllGc(Lists.newArrayList(GC.newBuilder().build()));

        JVMMetricCollection metrics = JVMMetricCollection.newBuilder()
                .setService("service")
                .setServiceInstance("service-instance")
                .addMetrics(jvmBuilder.build())
                .build();
        MessageExt messageExt = new MessageExt();
        messageExt.setBody(metrics.toByteArray());
        messageExt.setTopic(TOPIC_NAME);
        handler.handle(messageExt);
    }
}
