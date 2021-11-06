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

package org.skyapm.transporter.reporter.pulsar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminBuilder;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.skywalking.apm.agent.core.boot.BootService;
import org.apache.skywalking.apm.agent.core.boot.DefaultImplementor;
import org.apache.skywalking.apm.agent.core.boot.DefaultNamedThreadFactory;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.remote.GRPCChannelManager;
import org.apache.skywalking.apm.util.RunnableWithExceptionProtection;
import org.apache.skywalking.apm.util.StringUtil;

import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_AUTH_PARAMS;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_AUTH_PLUGIN_CLASSNAME;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_AUTO_CERT_REFRESH_TIME;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_CONNECTION_TIMEOUT;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_CONTEXT_CLASSLOADER;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_ENABLE_TLS_HOST_NAME_VERIFICATION;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_READ_TIMEOUT;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_REQUEST_TIMEOUT;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_SSL_PROVIDER;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_TLS_ALLOW_INSECURE_CONNECTION;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_TLS_CIPHERS;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_TLS_PROTOCOLS;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_TLS_TRUST_CERTS_FILE_PATH;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_TLS_TRUST_STORE_PASSWORD;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_TLS_TRUST_STORE_PATH;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_TLS_TRUST_STORE_TYPE;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.ADMIN_USE_KEY_STORE_TLS;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.CLIENT_SERVICE_URL;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.DOMAIN;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.NAMESPACE;
import static org.skyapm.transporter.reporter.pulsar.PulsarReporterPluginConfig.Plugin.Pulsar.WEB_SERVICE_URL;

/**
 * Configuring, initializing and holding Pulsar Producer instances for reporters.
 */
@DefaultImplementor
public class PulsarProducerManager implements BootService, Runnable {

    private static final ILog LOGGER = LogManager.getLogger(PulsarProducerManager.class);
    private static final Gson GSON = new Gson();

    private Set<String> topics = new HashSet<>();
    private List<PulsarConnectionStatusListener> listeners = new ArrayList<>();
    private volatile Map<String, Producer<byte[]>> producerMap = new HashMap<>();

    private volatile PulsarClient pulsarClient;

    private ScheduledFuture<?> bootProducerFuture;

    @Override
    public void prepare() {

    }

    @Override
    public void boot() {
        bootProducerFuture = Executors.newSingleThreadScheduledExecutor(
            new DefaultNamedThreadFactory("PulsarClientInitThread")
        ).scheduleAtFixedRate(new RunnableWithExceptionProtection(
                                  this,
                                  t -> LOGGER.error("unexpected exception.", t)
                              ), PulsarReporterPluginConfig.Plugin.Pulsar.PULSAR_CLIENT_SCHEDULE_AT_FIXED_RETE_INITIAL_DELAY,
                              PulsarReporterPluginConfig.Plugin.Pulsar.PULSAR_CLIENT_SCHEDULE_AT_FIXED_RETE_PERIOD,
                              PulsarReporterPluginConfig.Plugin.Pulsar.PULSAR_CLIENT_SCHEDULE_AT_FIXED_RETE_UNIT
        );
    }

    @Override
    public void run() {
        //Check whether all topics exist
        if (!checkTopics()) {
            return;
        }

        Map<String, Object> clientConfig = PulsarReporterPluginConfig.Plugin.Pulsar.getPulsarClientConfigMap();
        //create pulsar client
        try {
            LOGGER.debug("connect to pulsar cluster '{}', config is {}", CLIENT_SERVICE_URL, GSON.toJson(clientConfig));
            pulsarClient = PulsarClient.builder()
                                       .loadConf(clientConfig)
                                       .build();
        } catch (PulsarClientException e) {
            LOGGER.error(e, "connect to pulsar cluster '{}' failed, config is {}", CLIENT_SERVICE_URL,
                         GSON.toJson(clientConfig)
            );
            return;
        }

        //create producer for pulsar topic
        Map<String, Object> producerConfig = PulsarReporterPluginConfig.Plugin.Pulsar.getProducerDefaultConfig();
        Set<String> errorTopics = topics.stream().map(entry -> {
            Producer<byte[]> producer = getProducer(
                entry, producerConfig);
            if (producer == null) {
                return entry;
            } else {
                producerMap.put(entry, producer);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());

        if (!errorTopics.isEmpty()) {
            LOGGER.warn("create producer of topics {} failed, connect to pulsar cluster abort", errorTopics);
            try {
                shutdown();
            } catch (Throwable throwable) {
                LOGGER.error("close pulsar client failed", throwable);
            }
            return;
        }

        //notify listeners to send data if no exception been throw
        notifyListeners(PulsarConnectionStatus.CONNECTED);
        bootProducerFuture.cancel(true);
    }

    private boolean checkTopics() {
        //crate admin client
        PulsarAdmin admin = getAdmin();
        if (admin == null) {
            return false;
        }
        //check topics
        boolean checkResult;
        try {
            List<String> topicList = admin.topics().getList(NAMESPACE);
            if (topicList.isEmpty()) {
                LOGGER.warn("no pulsar topics in namespace {}, connect to pulsar cluster abort", NAMESPACE);
                checkResult = false;
            } else {
                List<String> missingTopics = topics.stream().filter(entry -> !topicList.contains(entry))
                                                   .collect(Collectors.toList());
                if (missingTopics.isEmpty()) {
                    checkResult = true;
                } else {
                    LOGGER.warn("pulsar topics {} are not exist, connect to pulsar cluster abort", missingTopics);
                    checkResult = false;
                }
            }
        } catch (PulsarAdminException e) {
            LOGGER.error(e, "get topic list from namespace {} failed, connect to pulsar cluster abort", NAMESPACE);
            checkResult = false;
        }
        admin.close();
        return checkResult;
    }

    private PulsarAdmin getAdmin() {
        try {
            PulsarAdminBuilder builder = PulsarAdmin.builder()
                                                    .serviceHttpUrl(WEB_SERVICE_URL)
                                                    .setContextClassLoader(ADMIN_CONTEXT_CLASSLOADER)
                                                    .autoCertRefreshTime(ADMIN_AUTO_CERT_REFRESH_TIME, TimeUnit.SECONDS)
                                                    .connectionTimeout(ADMIN_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                                                    .useKeyStoreTls(ADMIN_USE_KEY_STORE_TLS)
                                                    .enableTlsHostnameVerification(
                                                        ADMIN_ENABLE_TLS_HOST_NAME_VERIFICATION)
                                                    .tlsTrustCertsFilePath(ADMIN_TLS_TRUST_CERTS_FILE_PATH)
                                                    .allowTlsInsecureConnection(ADMIN_TLS_ALLOW_INSECURE_CONNECTION)
                                                    .readTimeout(ADMIN_READ_TIMEOUT, TimeUnit.SECONDS)
                                                    .requestTimeout(ADMIN_REQUEST_TIMEOUT, TimeUnit.SECONDS)
                                                    .sslProvider(ADMIN_SSL_PROVIDER)
                                                    .tlsCiphers(getSetFromJson(ADMIN_TLS_CIPHERS))
                                                    .tlsProtocols(getSetFromJson(ADMIN_TLS_PROTOCOLS))
                                                    .tlsTrustStorePassword(ADMIN_TLS_TRUST_STORE_PASSWORD)
                                                    .tlsTrustStorePath(ADMIN_TLS_TRUST_STORE_PATH)
                                                    .tlsTrustStoreType(ADMIN_TLS_TRUST_STORE_TYPE);
            if (StringUtil.isNotBlank(ADMIN_AUTH_PLUGIN_CLASSNAME)) {
                builder.authentication(ADMIN_AUTH_PLUGIN_CLASSNAME, ADMIN_AUTH_PARAMS);
            }
            return builder.build();
        } catch (PulsarClientException e) {
            LOGGER.error(e, "create pulsar admin client from '{}' failed", WEB_SERVICE_URL);
            return null;
        }
    }

    private Set<String> getSetFromJson(String json) {
        Type type = new TypeToken<HashSet<String>>() {
        }.getType();
        return GSON.fromJson(json, type);
    }

    private void notifyListeners(PulsarConnectionStatus status) {
        for (PulsarConnectionStatusListener listener : listeners) {
            listener.onStatusChanged(status);
        }
    }

    String formatTopicNameThenRegister(String topic) {
        StringBuilder topicName = new StringBuilder(DOMAIN).append("://").append(NAMESPACE).append('/').append(topic);
        topics.add(topicName.toString());
        return topicName.toString();
    }

    public void addListener(PulsarConnectionStatusListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Get the Pulsar Producer instance to send data to Pulsar broker.
     */
    public final Producer<byte[]> getProducer(String topic) {
        return producerMap.get(topic);
    }

    /**
     * Get the Pulsar Producer instance to send data to Pulsar broker.
     */
    private Producer<byte[]> getProducer(String topic, Map<String, Object> conf) {
        try {
            return pulsarClient.newProducer()
                               .loadConf(conf)
                               .topic(topic)
                               .create();
        } catch (PulsarClientException e) {
            LOGGER.error(e, "create the producer of topic '{}' failed", topic);
        }
        return null;
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void shutdown() throws Throwable {
        pulsarClient.close();
    }

    @Override
    public int priority() {
        return ServiceManager.INSTANCE.findService(GRPCChannelManager.class).priority() - 1;
    }
}