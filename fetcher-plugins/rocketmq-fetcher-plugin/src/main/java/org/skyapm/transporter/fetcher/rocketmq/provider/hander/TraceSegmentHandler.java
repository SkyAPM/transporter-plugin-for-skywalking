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

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.skywalking.apm.network.language.agent.v3.SegmentObject;
import org.apache.skywalking.oap.server.analyzer.module.AnalyzerModule;
import org.apache.skywalking.oap.server.analyzer.provider.trace.parser.ISegmentParserService;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;
import org.apache.skywalking.oap.server.telemetry.api.CounterMetrics;
import org.apache.skywalking.oap.server.telemetry.api.HistogramMetrics;
import org.apache.skywalking.oap.server.telemetry.api.MetricsCreator;
import org.apache.skywalking.oap.server.telemetry.api.MetricsTag;
import org.skyapm.transporter.fetcher.rocketmq.module.RocketmqFetcherConfig;

/**
 * A handler deserializes the message of the trace segment data and pushes it to downstream.
 */
@Slf4j
public class TraceSegmentHandler extends AbstractRocketmqHandler {
    private final ISegmentParserService segmentParserService;

    private final HistogramMetrics histogram;
    private final CounterMetrics errorCounter;

    public TraceSegmentHandler(ModuleManager moduleManager, RocketmqFetcherConfig config) {
        super(moduleManager, config);
        this.segmentParserService = moduleManager.find(AnalyzerModule.NAME)
                .provider()
                .getService(ISegmentParserService.class);

        MetricsCreator metricsCreator = moduleManager.find(TelemetryModule.NAME)
                .provider().getService(MetricsCreator.class);

        histogram = metricsCreator.createHistogramMetric(
                "trace_in_latency",
                "The process latency of trace data",
                new MetricsTag.Keys("protocol"),
                new MetricsTag.Values("rocketmq")
        );
        errorCounter = metricsCreator.createCounter(
                "trace_analysis_error_count",
                "The error number of trace analysis",
                new MetricsTag.Keys("protocol"),
                new MetricsTag.Values("rocketmq")
        );
    }

    @Override
    public void handle(final MessageExt message) {
        try (HistogramMetrics.Timer ignore = histogram.createTimer()) {
            SegmentObject segment = SegmentObject.parseFrom(message.getBody());
            if (log.isDebugEnabled()) {
                log.debug(
                        "Fetched a tracing segment[{}] from service instance[{}].",
                        segment.getTraceSegmentId(),
                        segment.getServiceInstance()
                );
            }
            segmentParserService.send(segment);
        } catch (InvalidProtocolBufferException e) {
            errorCounter.inc();
            log.error("handle record failed", e);
        }
    }

    @Override
    protected String getPlainTopic() {
        return config.getTopicNameOfTracingSegments();
    }
}
