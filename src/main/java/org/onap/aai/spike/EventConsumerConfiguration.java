/**
 * ﻿============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017-2018 Amdocs
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.spike;

import java.net.MalformedURLException;
import org.onap.aai.event.client.DMaaPEventConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("file:${CONFIG_HOME}/event-consumer.properties")
public class EventConsumerConfiguration {

    //@formatter:off
    @Bean
    DMaaPEventConsumer dmaapEventConsumer( //NOSONAR
        @Value("${event.consumer.host}") String host,
        @Value("${event.consumer.topic}") String topic,
        @Value("${event.consumer.username}") String username,
        @Value("${event.consumer.password}") String password,
        @Value("${event.consumer.consumerGroup}") String consumerGroup,
        @Value("${event.consumer.consumerId}") String consumerId,
        @Value("${event.consumer.timeoutMs}") int timeoutMs,
        @Value("${event.consumer.messageLimit}") int messageLimit,
        @Value("${event.consumer.transportType}") String transportType,
        @Value("${event.consumer.protocol}") String protocol) throws MalformedURLException
    {
    //@formatter:on
        return new DMaaPEventConsumer(host, topic, username, password, consumerGroup, consumerId, timeoutMs,
                messageLimit, transportType, protocol, null);
    }

}
