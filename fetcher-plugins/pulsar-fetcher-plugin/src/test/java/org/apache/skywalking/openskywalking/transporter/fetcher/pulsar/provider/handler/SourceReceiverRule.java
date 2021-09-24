package org.apache.skywalking.openskywalking.transporter.fetcher.pulsar.provider.handler;

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
