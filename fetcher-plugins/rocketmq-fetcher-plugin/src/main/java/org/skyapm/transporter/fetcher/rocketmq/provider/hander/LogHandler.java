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
import org.apache.skywalking.apm.network.logging.v3.LogData;
import org.apache.skywalking.oap.log.analyzer.module.LogAnalyzerModule;
import org.apache.skywalking.oap.log.analyzer.provider.log.ILogAnalyzerService;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;
import org.apache.skywalking.oap.server.telemetry.api.CounterMetrics;
import org.apache.skywalking.oap.server.telemetry.api.HistogramMetrics;
import org.apache.skywalking.oap.server.telemetry.api.MetricsCreator;
import org.apache.skywalking.oap.server.telemetry.api.MetricsTag;
import org.skyapm.transporter.fetcher.rocketmq.module.RocketmqFetcherConfig;

@Slf4j
public class LogHandler extends AbstractRocketmqHandler {

    private final RocketmqFetcherConfig config;
    private final HistogramMetrics histogram;
    private final CounterMetrics errorCounter;
    private final ILogAnalyzerService logAnalyzerService;

    public LogHandler(ModuleManager moduleManager, RocketmqFetcherConfig config) {
        super(moduleManager, config);
        this.config = config;
        this.logAnalyzerService = moduleManager.find(LogAnalyzerModule.NAME)
                .provider()
                .getService(ILogAnalyzerService.class);

        MetricsCreator metricsCreator = moduleManager.find(TelemetryModule.NAME)
                .provider()
                .getService(MetricsCreator.class);
        histogram = metricsCreator.createHistogramMetric(
                "log_in_latency",
                "The process latency of log",
                new MetricsTag.Keys("protocol", "data_format"),
                new MetricsTag.Values("org/skyapm/transporter/fetcher/rocketmq", getDataFormat())
        );
        errorCounter = metricsCreator.createCounter(
                "log_analysis_error_count",
                "The error number of log analysis",
                new MetricsTag.Keys("protocol", "data_format"),
                new MetricsTag.Values("org/skyapm/transporter/fetcher/rocketmq", getDataFormat())
        );
    }

    @Override
    protected String getPlainTopic() {
        return config.getTopicNameOfMetrics();
    }

    @Override
    public void handle(final MessageExt messageExt) {
        try (HistogramMetrics.Timer ignore = histogram.createTimer()) {
            LogData logData = parseConsumerRecord(messageExt);
            logAnalyzerService.doAnalysis(logData, null);
        } catch (Exception e) {
            errorCounter.inc();
            log.error(e.getMessage(), e);
        }
    }

    protected String getDataFormat() {
        return "protobuf";
    }

    protected LogData parseConsumerRecord(MessageExt message) throws Exception {
        return LogData.parseFrom(message.getBody());
    }
}
