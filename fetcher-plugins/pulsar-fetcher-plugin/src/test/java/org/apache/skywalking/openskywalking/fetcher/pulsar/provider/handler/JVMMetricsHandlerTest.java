package org.apache.skywalking.openskywalking.fetcher.pulsar.provider.handler;

import com.google.common.collect.Lists;
import java.util.List;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.apache.skywalking.openskywalking.fetcher.pulsar.module.PulsarFetcherConfig;
import org.apache.skywalking.openskywalking.fetcher.pulsar.mock.MockMessage;
import org.apache.skywalking.openskywalking.fetcher.pulsar.mock.MockModuleManager;
import org.apache.skywalking.openskywalking.fetcher.pulsar.mock.MockModuleProvider;

import static org.hamcrest.CoreMatchers.is;

public class JVMMetricsHandlerTest {
    private static final String TOPIC_NAME = "persistent://public/default/skywalking-metrics";
    private JVMMetricsHandler handler = null;
    private PulsarFetcherConfig config = new PulsarFetcherConfig();

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
        handler.handle(new MockMessage(TOPIC_NAME, metrics.toByteArray(), ""));
    }

}