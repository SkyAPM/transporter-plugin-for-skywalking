package org.apache.skywalking.openskywalking.transporter.fetcher.pulsar.provider;

import org.apache.skywalking.oap.server.library.module.ModuleConfig;
import org.junit.Test;
import org.apache.skywalking.openskywalking.transporter.fetcher.pulsar.module.PulsarFetcherConfig;
import org.apache.skywalking.openskywalking.transporter.fetcher.pulsar.module.PulsarFetcherModule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PulsarFetcherProviderTest {
    PulsarFetcherProvider provider = new PulsarFetcherProvider();

    @Test
    public void name() {
        assertEquals("default", provider.name());
    }

    @Test
    public void module() {
        assertEquals(PulsarFetcherModule.class, provider.module());
    }

    @Test
    public void createConfigBeanIfAbsent() {
        ModuleConfig moduleConfig = provider.createConfigBeanIfAbsent();
        assertTrue(moduleConfig instanceof PulsarFetcherConfig);
    }
}