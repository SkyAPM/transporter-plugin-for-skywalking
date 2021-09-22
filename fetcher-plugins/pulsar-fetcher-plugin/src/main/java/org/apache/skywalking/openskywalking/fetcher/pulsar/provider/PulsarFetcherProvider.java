/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.skywalking.openskywalking.fetcher.pulsar.provider;

import org.apache.skywalking.oap.log.analyzer.module.LogAnalyzerModule;
import org.apache.skywalking.openskywalking.fetcher.pulsar.PulsarFetcherHandlerRegister;
import org.apache.skywalking.openskywalking.fetcher.pulsar.provider.handler.MeterServiceHandler;
import org.apache.skywalking.openskywalking.fetcher.pulsar.provider.handler.ProfileTaskHandler;
import org.apache.skywalking.openskywalking.fetcher.pulsar.provider.handler.TraceSegmentHandler;
import org.apache.skywalking.openskywalking.fetcher.pulsar.module.PulsarFetcherConfig;
import org.apache.skywalking.openskywalking.fetcher.pulsar.module.PulsarFetcherModule;
import org.apache.skywalking.openskywalking.fetcher.pulsar.provider.handler.JVMMetricsHandler;
import org.apache.skywalking.openskywalking.fetcher.pulsar.provider.handler.JsonLogHandler;
import org.apache.skywalking.openskywalking.fetcher.pulsar.provider.handler.LogHandler;
import org.apache.skywalking.openskywalking.fetcher.pulsar.provider.handler.ServiceManagementHandler;
import org.apache.skywalking.oap.server.analyzer.module.AnalyzerModule;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;
import org.apache.skywalking.oap.server.library.module.ModuleDefine;
import org.apache.skywalking.oap.server.library.module.ModuleProvider;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.module.ServiceNotProvidedException;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;

public class PulsarFetcherProvider extends ModuleProvider {
    private PulsarFetcherHandlerRegister handlerRegister;
    private PulsarFetcherConfig config;

    public PulsarFetcherProvider() {
        config = new PulsarFetcherConfig();
    }

    @Override
    public String name() {
        return "default";
    }

    @Override
    public Class<? extends ModuleDefine> module() {
        return PulsarFetcherModule.class;
    }

    @Override
    public ModuleConfig createConfigBeanIfAbsent() {
        return config;
    }

    @Override
    public void prepare() throws ServiceNotProvidedException, ModuleStartException {
        handlerRegister = new PulsarFetcherHandlerRegister(config);
    }

    @Override
    public void start() throws ServiceNotProvidedException, ModuleStartException {
        handlerRegister.register(new JVMMetricsHandler(getManager(), config));
        handlerRegister.register(new ServiceManagementHandler(getManager(), config));
        handlerRegister.register(new TraceSegmentHandler(getManager(), config));
        handlerRegister.register(new ProfileTaskHandler(getManager(), config));
        handlerRegister.register(new MeterServiceHandler(getManager(), config));

        if (config.isEnableNativeProtoLog()) {
            handlerRegister.register(new LogHandler(getManager(), config));
        }
        if (config.isEnableNativeJsonLog()) {
            handlerRegister.register(new JsonLogHandler(getManager(), config));
        }

        handlerRegister.start();
    }

    @Override
    public void notifyAfterCompleted() throws ServiceNotProvidedException, ModuleStartException {

    }

    @Override
    public String[] requiredModules() {
        return new String[] {
            TelemetryModule.NAME,
            AnalyzerModule.NAME,
            LogAnalyzerModule.NAME,
            CoreModule.NAME
        };
    }
}
