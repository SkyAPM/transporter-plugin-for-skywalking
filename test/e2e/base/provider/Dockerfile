#
# Copyright 2021 SkyAPM
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#

FROM apache/skywalking-java-agent:8.7.0-jdk8

COPY ./reporter-plugins/pulsar-reporter-plugin/target/pulsar-reporter-plugin-*.*.*-shaded.jar /skywalking/agent/plugins/

VOLUME /services
COPY ./test/e2e/base/provider/agent.config /skywalking/agent/config/agent.config
ADD ./test/e2e/base/provider/target/e2e-service-provider.jar /services/

ENV JAVA_OPTS=""

CMD ["sh", "-c", "java $JAVA_OPTS -jar /services/e2e-service-provider.jar"]
