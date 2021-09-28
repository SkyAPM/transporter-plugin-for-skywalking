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

package org.skyapm.transporter.fetcher.pulsar.mock;

import com.google.common.collect.Maps;
import org.apache.skywalking.oap.server.library.module.ModuleServiceHolder;
import org.apache.skywalking.oap.server.library.module.Service;
import org.apache.skywalking.oap.server.library.module.ServiceNotProvidedException;

import java.util.Map;

public abstract class MockModuleProvider implements ModuleServiceHolder {
    protected Map<Class<? extends Service>, Service> serviceMap = Maps.newHashMap();

    public MockModuleProvider() {
        register();
    }

    protected abstract void register();

    @Override
    public void registerServiceImplementation(final Class<? extends Service> serviceType,
                                              final Service service) throws ServiceNotProvidedException {
        serviceMap.put(serviceType, service);
    }

    @Override
    public <T extends Service> T getService(final Class<T> serviceType) throws ServiceNotProvidedException {
        return (T) serviceMap.get(serviceType);
    }
}
