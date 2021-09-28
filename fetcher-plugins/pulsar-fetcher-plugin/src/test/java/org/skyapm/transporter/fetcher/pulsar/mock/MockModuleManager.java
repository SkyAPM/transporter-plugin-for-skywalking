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
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.module.ModuleNotFoundRuntimeException;
import org.apache.skywalking.oap.server.library.module.ModuleProviderHolder;

import java.util.Map;

public abstract class MockModuleManager extends ModuleManager {
    private final Map<String, ModuleProviderHolder> moduleProviderHolderMap = Maps.newHashMap();

    public MockModuleManager() {
        init();
    }

    protected abstract void init();

    protected void register(String name, ModuleProviderHolder provider) {
        moduleProviderHolderMap.put(name, provider);
    }

    public boolean has(String moduleName) {
        return moduleProviderHolderMap.containsKey(moduleName);
    }

    public ModuleProviderHolder find(String moduleName) throws ModuleNotFoundRuntimeException {
        if (!moduleProviderHolderMap.containsKey(moduleName)) {
            throw new ModuleNotFoundRuntimeException("ModuleProviderHolder[" + moduleName + "] cannot found in MOCK.");
        }
        return moduleProviderHolderMap.get(moduleName);
    }
}
