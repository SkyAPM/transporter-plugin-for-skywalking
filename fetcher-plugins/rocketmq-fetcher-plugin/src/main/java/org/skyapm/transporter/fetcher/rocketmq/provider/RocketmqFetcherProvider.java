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

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.log.analyzer.module.LogAnalyzerModule;
import org.apache.skywalking.oap.server.analyzer.module.AnalyzerModule;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;
import org.apache.skywalking.oap.server.library.module.ModuleDefine;
import org.apache.skywalking.oap.server.library.module.ModuleProvider;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.module.ServiceNotProvidedException;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;
import org.skyapm.transporter.fetcher.rocketmq.RocketmqFetcherHandlerRegister;
import org.skyapm.transporter.fetcher.rocketmq.module.RocketmqFetcherConfig;
import org.skyapm.transporter.fetcher.rocketmq.module.RocketmqFetcherModule;
import org.skyapm.transporter.fetcher.rocketmq.provider.hander.RocketmqJVMMetricsHandler;
import org.skyapm.transporter.fetcher.rocketmq.provider.hander.RocketmqJsonLogHandler;
import org.skyapm.transporter.fetcher.rocketmq.provider.hander.RocketmqLogHandler;
import org.skyapm.transporter.fetcher.rocketmq.provider.hander.RocketmqMeterServiceHandler;
import org.skyapm.transporter.fetcher.rocketmq.provider.hander.RocketmqProfileTaskHandler;
import org.skyapm.transporter.fetcher.rocketmq.provider.hander.RocketmqServiceManagementHandler;
import org.skyapm.transporter.fetcher.rocketmq.provider.hander.RocketmqTraceSegmentHandler;

@Slf4j
public class RocketmqFetcherProvider extends ModuleProvider {
    private RocketmqFetcherHandlerRegister handlerRegister;
    private RocketmqFetcherConfig config;

    public RocketmqFetcherProvider() {
        config = new RocketmqFetcherConfig();
    }

    @Override
    public String name() {
        return "default";
    }

    @Override
    public Class<? extends ModuleDefine> module() {
        return RocketmqFetcherModule.class;
    }

    @Override
    public ModuleConfig createConfigBeanIfAbsent() {
        return config;
    }

    @Override
    public void prepare() throws ServiceNotProvidedException, ModuleStartException {
        handlerRegister = new RocketmqFetcherHandlerRegister(config);
    }

    @Override
    public void start() throws ServiceNotProvidedException, ModuleStartException {
        handlerRegister.register(new RocketmqJVMMetricsHandler(getManager(), config));
        handlerRegister.register(new RocketmqServiceManagementHandler(getManager(), config));
        handlerRegister.register(new RocketmqTraceSegmentHandler(getManager(), config));
        handlerRegister.register(new RocketmqProfileTaskHandler(getManager(), config));
        handlerRegister.register(new RocketmqMeterServiceHandler(getManager(), config));

        if (config.isEnableNativeProtoLog()) {
            handlerRegister.register(new RocketmqLogHandler(getManager(), config));
        }
        if (config.isEnableNativeJsonLog()) {
            handlerRegister.register(new RocketmqJsonLogHandler(getManager(), config));
        }

        handlerRegister.start();
    }

    @Override
    public void notifyAfterCompleted() throws ServiceNotProvidedException {
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
