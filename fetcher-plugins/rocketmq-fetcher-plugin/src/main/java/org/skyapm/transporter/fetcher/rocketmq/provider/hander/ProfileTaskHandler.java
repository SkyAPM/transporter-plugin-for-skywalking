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
import org.apache.skywalking.apm.network.language.profile.v3.ThreadSnapshot;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.worker.RecordStreamProcessor;
import org.apache.skywalking.oap.server.core.profile.ProfileThreadSnapshotRecord;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;
import org.apache.skywalking.oap.server.telemetry.api.CounterMetrics;
import org.apache.skywalking.oap.server.telemetry.api.HistogramMetrics;
import org.apache.skywalking.oap.server.telemetry.api.MetricsCreator;
import org.apache.skywalking.oap.server.telemetry.api.MetricsTag;
import org.skyapm.transporter.fetcher.rocketmq.module.RocketmqFetcherConfig;

/**
 * A handler deserializes the message of profiling snapshot and pushes it to downstream.
 */
@Slf4j
public class ProfileTaskHandler extends AbstractRocketmqHandler {
    private final HistogramMetrics histogram;
    private final CounterMetrics errorCounter;

    public ProfileTaskHandler(ModuleManager manager, RocketmqFetcherConfig config) {
        super(manager, config);
        MetricsCreator metricsCreator = manager.find(TelemetryModule.NAME)
                .provider()
                .getService(MetricsCreator.class);
        histogram = metricsCreator.createHistogramMetric(
                "profile_task_in_latency",
                "The process latency of profile task",
                new MetricsTag.Keys("protocol"),
                new MetricsTag.Values("rocketmq")
        );
        errorCounter = metricsCreator.createCounter(
                "profile_task_analysis_error_count",
                "The error number of profile task process",
                new MetricsTag.Keys("protocol"),
                new MetricsTag.Values("rocketmq")
        );
    }

    @Override
    public void handle(final MessageExt message) {
        try (HistogramMetrics.Timer ignored = histogram.createTimer()) {
            ThreadSnapshot snapshot = ThreadSnapshot.parseFrom(message.getBody());
            if (log.isDebugEnabled()) {
                log.debug(
                        "Fetched a thread snapshot[{}] from task[{}] reported",
                        snapshot.getTraceSegmentId(),
                        snapshot.getTaskId()
                );
            }

            final ProfileThreadSnapshotRecord snapshotRecord = new ProfileThreadSnapshotRecord();
            snapshotRecord.setTaskId(snapshot.getTaskId());
            snapshotRecord.setSegmentId(snapshot.getTraceSegmentId());
            snapshotRecord.setDumpTime(snapshot.getTime());
            snapshotRecord.setSequence(snapshot.getSequence());
            snapshotRecord.setStackBinary(snapshot.getStack().toByteArray());
            snapshotRecord.setTimeBucket(TimeBucket.getRecordTimeBucket(snapshot.getTime()));

            RecordStreamProcessor.getInstance().in(snapshotRecord);
        } catch (Exception e) {
            errorCounter.inc();
            log.error("handle record failed", e);
        }
    }

    @Override
    protected String getPlainTopic() {
        return config.getTopicNameOfProfiling();
    }
}
