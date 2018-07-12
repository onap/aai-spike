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
package org.onap.aai.spike.event.envelope;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.onap.aai.spike.event.outgoing.SpikeGraphEvent;

public class EventEnvelope {

    private EventHeader header;
    private SpikeGraphEvent body;

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public EventEnvelope(EventHeader eventHeader, SpikeGraphEvent body) {
        this.header = eventHeader;
        this.body = body;
    }

    /**
     * Construct the envelope header from the provided event.
     *
     * @param event the Spike graph event for a vertex or edge operation
     */
    public EventEnvelope(SpikeGraphEvent event) {
        this.header = new EventHeader.Builder().requestId(event.getTransactionId()).build();
        this.body = event;
    }

    public EventHeader getEventHeader() {
        return header;
    }

    public SpikeGraphEvent getBody() {
        return body;
    }

    /**
     * Serializes this object into a JSON string representation.
     *
     * @return a JSON format string representation of this object.
     */
    public String toJson() {
        return gson.toJson(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.toJson();
    }
}
