package org.apache.skywalking.openskywalking.fetcher.pulsar.provider.handler;

import java.util.List;
import org.apache.skywalking.apm.network.management.v3.InstancePingPkg;
import org.apache.skywalking.apm.network.management.v3.InstanceProperties;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.config.NamingControl;
import org.apache.skywalking.oap.server.core.config.group.EndpointNameGrouping;
import org.apache.skywalking.oap.server.core.source.ISource;
import org.apache.skywalking.oap.server.core.source.ServiceInstanceUpdate;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.apache.skywalking.openskywalking.fetcher.pulsar.module.PulsarFetcherConfig;
import org.apache.skywalking.openskywalking.fetcher.pulsar.mock.MockMessage;
import org.apache.skywalking.openskywalking.fetcher.pulsar.mock.MockModuleManager;
import org.apache.skywalking.openskywalking.fetcher.pulsar.mock.MockModuleProvider;

public class ServiceManagementHandlerTest {
    private static final String TOPIC_NAME = "persistent://public/default/skywalking-managements";

    private static final String SERVICE = "MOCK_SERVER";
    private static final String SERVICE_INSTANCE = "MOCK_SERVICE_INSTANCE";
    private PulsarHandler handler = null;
    private PulsarFetcherConfig config = new PulsarFetcherConfig();

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

        handler.handle(new MockMessage(TOPIC_NAME, properties.toByteArray(), "register"));
        handler.handle(
            new MockMessage(TOPIC_NAME, ping.toByteArray(), ping.getServiceInstance()));
    }

}