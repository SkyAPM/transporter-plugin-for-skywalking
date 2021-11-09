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

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.skywalking.apm.network.management.v3.InstancePingPkg;
import org.apache.skywalking.apm.network.management.v3.InstanceProperties;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.config.NamingControl;
import org.apache.skywalking.oap.server.core.config.group.EndpointNameGrouping;
import org.apache.skywalking.oap.server.core.source.ISource;
import org.apache.skywalking.oap.server.core.source.ServiceInstanceUpdate;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.skyapm.transporter.fetcher.rocketmq.mock.MockModuleManager;
import org.skyapm.transporter.fetcher.rocketmq.mock.MockModuleProvider;
import org.skyapm.transporter.fetcher.rocketmq.module.RocketmqFetcherConfig;
import org.skyapm.transporter.fetcher.rocketmq.provider.hander.RocketmqHandler;
import org.skyapm.transporter.fetcher.rocketmq.provider.hander.ServiceManagementHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

public class ServiceManagementHandlerTest {
    private static final String TOPIC_NAME = "skywalking-managements";

    private static final String SERVICE = "MOCK_SERVER";
    private static final String SERVICE_INSTANCE = "MOCK_SERVICE_INSTANCE";
    private RocketmqHandler handler = null;
    private RocketmqFetcherConfig config = new RocketmqFetcherConfig();

    private ModuleManager manager;

    @ClassRule
    public static SourceReceiverRule SOURCE_RECEIVER = new SourceReceiverRule() {

        @Override
        protected void verify(final List<ISource> sourceList) throws Throwable {
            ServiceInstanceUpdate instanceUpdate = (ServiceInstanceUpdate) sourceList.get(0);
            Assert.assertEquals(instanceUpdate.getName(), SERVICE_INSTANCE);

            ServiceInstanceUpdate instanceUpdate1 = (ServiceInstanceUpdate) sourceList.get(1);
            Assert.assertEquals(instanceUpdate1.getName(), SERVICE_INSTANCE);
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
            }
        };
        handler = new ServiceManagementHandler(manager, config);
    }

    @Test
    public void testTopicName() {
        Assert.assertEquals(handler.getTopic(), TOPIC_NAME);
    }

    @Test
    public void testHandler() {
        InstanceProperties properties = InstanceProperties.newBuilder()
                .setService(SERVICE)
                .setServiceInstance(SERVICE_INSTANCE)
                .build();
        InstancePingPkg ping = InstancePingPkg.newBuilder()
                .setService(SERVICE)
                .setServiceInstance(SERVICE_INSTANCE)
                .build();

        MessageExt messageExt = new MessageExt();
        messageExt.setTopic(TOPIC_NAME);
        messageExt.setTags("register-" + SERVICE_INSTANCE);
        messageExt.setBody(properties.toByteArray());

        MessageExt messageExt2 = new MessageExt();
        messageExt2.setTopic(TOPIC_NAME);
        messageExt2.setTags("register-" + SERVICE_INSTANCE);
        messageExt2.setBody(ping.toByteArray());
        handler.handle(messageExt);
        handler.handle(messageExt2);
    }
}
