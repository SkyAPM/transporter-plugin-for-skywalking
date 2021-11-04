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

package org.skyapm.transporter.fetcher.rocketmq.provider.hander;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.skywalking.apm.network.language.agent.v3.JVMMetricCollection;
import org.apache.skywalking.oap.server.analyzer.provider.jvm.JVMSourceDispatcher;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.config.NamingControl;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;
import org.apache.skywalking.oap.server.telemetry.api.CounterMetrics;
import org.apache.skywalking.oap.server.telemetry.api.HistogramMetrics;
import org.apache.skywalking.oap.server.telemetry.api.MetricsCreator;
import org.apache.skywalking.oap.server.telemetry.api.MetricsTag;
import org.skyapm.transporter.fetcher.rocketmq.module.RocketmqFetcherConfig;

/**
 * A handler deserializes the message of JVM Metrics and pushes it to downstream.
 */
@Slf4j
public class RocketmqJVMMetricsHandler extends AbstractRocketmqHandler {
    private final NamingControl namingLengthControl;
    private final JVMSourceDispatcher jvmSourceDispatcher;

    private final HistogramMetrics histogram;
    private final HistogramMetrics histogramBatch;
    private final CounterMetrics errorCounter;

    public RocketmqJVMMetricsHandler(ModuleManager manager, RocketmqFetcherConfig config) {
        super(manager, config);
        this.jvmSourceDispatcher = new JVMSourceDispatcher(manager);
        this.namingLengthControl = manager.find(CoreModule.NAME)
                .provider()
                .getService(NamingControl.class);
        MetricsCreator metricsCreator = manager.find(TelemetryModule.NAME)
                .provider()
                .getService(MetricsCreator.class);
        histogram = metricsCreator.createHistogramMetric(
                "meter_in_latency",
                "The process latency of meter",
                new MetricsTag.Keys("protocol"),
                new MetricsTag.Values("org/skyapm/transporter/fetcher/rocketmq")
        );
        histogramBatch = metricsCreator.createHistogramMetric(
                "meter_in_latency",
                "The process latency of meter",
                new MetricsTag.Keys("protocol"),
                new MetricsTag.Values("org/skyapm/transporter/fetcher/rocketmq")
        );
        errorCounter = metricsCreator.createCounter(
                "meter_analysis_error_count",
                "The error number of meter analysis",
                new MetricsTag.Keys("protocol"),
                new MetricsTag.Values("org/skyapm/transporter/fetcher/rocketmq")
        );
    }

    @Override
    public void handle(final MessageExt message) {
        try (HistogramMetrics.Timer ignored = histogramBatch.createTimer()) {
            JVMMetricCollection metrics = JVMMetricCollection.parseFrom(message.getBody());

            if (log.isDebugEnabled()) {
                log.debug(
                        "Fetched JVM metrics from service[{}] instance[{}] reported.",
                        metrics.getService(),
                        metrics.getServiceInstance()
                );
            }
            JVMMetricCollection.Builder builder = metrics.toBuilder();
            builder.setService(namingLengthControl.formatServiceName(builder.getService()));
            builder.setServiceInstance(namingLengthControl.formatInstanceName(builder.getServiceInstance()));

            builder.getMetricsList().forEach(jvmMetric -> {
                try (HistogramMetrics.Timer timer = histogram.createTimer()) {
                    jvmSourceDispatcher.sendMetric(builder.getService(), builder.getServiceInstance(), jvmMetric);
                } catch (Exception e) {
                    errorCounter.inc();
                    log.error(e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("handle record failed", e);
        }
    }

    @Override
    protected String getPlainTopic() {
        return config.getTopicNameOfMetrics();
    }
}
