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

import org.onap.aai.event.client.DMaaPEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("file:${CONFIG_HOME}/event-publisher.properties")
public class EventPublisherConfiguration {

    //@formatter:off
    @Bean
    DMaaPEventPublisher dmaapEventPublisher( //NOSONAR
        @Value("${event.publisher.host}") String host,
        @Value("${event.publisher.topic}") String topic,
        @Value("${event.publisher.username}") String username,
        @Value("${event.publisher.password}") String password,
        @Value("${event.publisher.maxBatchSize}") int maxBatchSize,
        @Value("${event.publisher.maxAgeMs}") long maxAgeMs,
        @Value("${event.publisher.delayBetweenBatchesMs}") int delayBetweenBatchesMs,
        @Value("${event.publisher.transportType}") String transportType)
    {
    //@formatter:on
        return new DMaaPEventPublisher(host, topic, username, password, maxBatchSize, maxAgeMs, delayBetweenBatchesMs,
                transportType);
    }

}
