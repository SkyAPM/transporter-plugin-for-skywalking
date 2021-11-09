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

import com.google.common.collect.Lists;
import org.apache.skywalking.oap.server.core.analysis.DispatcherDetectorListener;
import org.apache.skywalking.oap.server.core.source.ISource;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.junit.rules.Verifier;

import java.util.List;

public abstract class SourceReceiverRule extends Verifier implements SourceReceiver {
    private final List<ISource> sourceList = Lists.newArrayList();

    @Override
    public void receive(final ISource source) {
        sourceList.add(source);
    }

    @Override
    public DispatcherDetectorListener getDispatcherDetectorListener() {
        return null;
    }

    @Override
    protected void verify() throws Throwable {
        verify(sourceList);
    }

    protected abstract void verify(List<ISource> sourceList) throws Throwable;

}
