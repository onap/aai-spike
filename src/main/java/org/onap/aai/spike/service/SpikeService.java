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
package org.onap.aai.spike.service;

import java.util.Timer;
import javax.annotation.PreDestroy;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.event.api.EventConsumer;
import org.onap.aai.event.api.EventPublisher;
import org.onap.aai.spike.logging.SpikeMsgs;
import org.onap.aai.spike.schema.EdgeRulesLoader;
import org.onap.aai.spike.schema.OXMModelLoader;
import org.onap.aai.spike.util.SpikeConstants;
import org.onap.aai.spike.util.SpikeProperties;

public class SpikeService {

    private EventConsumer consumer;
    private EventPublisher publisher;
    private static Logger logger = LoggerFactory.getInstance().getLogger(SpikeService.class.getName());
    private Timer timer;

    public SpikeService(EventConsumer consumer, EventPublisher publisher) {
        this.consumer = consumer;
        this.publisher = publisher;
    }

    public void startup() throws Exception {

        // Load models
        EdgeRulesLoader.loadModels();
        OXMModelLoader.loadModels();

        Long interval = 30000L;
        try {
            interval = Long.parseLong(SpikeProperties.get(SpikeConstants.SPIKE_EVENT_POLL_INTERVAL));
        } catch (Exception ex) {
        }

        SpikeEventProcessor processEvent = new SpikeEventProcessor(consumer, publisher);
        timer = new Timer("spike-consumer");
        timer.schedule(processEvent, interval, interval);

        logger.info(SpikeMsgs.SPIKE_SERVICE_STARTED_SUCCESSFULLY, consumer.getClass().getName());
    }

    @PreDestroy
    protected void preShutdown() {
        logger.info(SpikeMsgs.SPIKE_SERVICE_STARTED_SUCCESSFULLY, consumer.getClass().getName());
        timer.cancel();
    }

}
