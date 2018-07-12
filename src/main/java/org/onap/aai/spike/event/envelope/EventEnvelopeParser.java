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

import java.util.UUID;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.onap.aai.spike.event.incoming.GizmoGraphEvent;
import org.onap.aai.spike.exception.SpikeException;
import org.onap.aai.spike.schema.GraphEventTransformer;

public class EventEnvelopeParser {

    /**
     * Parses the event to extract the content of the body and generate a {@link GizmoGraphEvent}
     * object.
     *
     * @param event event envelope with both header and body properties
     * @return the body of the event represented by a {@link GizmoGraphEvent} object
     * @throws SpikeException if the event cannot be parsed
     */
    public GizmoGraphEvent parseEvent(String event) throws SpikeException {

        GizmoGraphEvent graphEvent = GizmoGraphEvent.fromJson(extractEventBody(event));

        GraphEventTransformer.populateUUID(graphEvent);
        if (graphEvent.getTransactionId() == null || graphEvent.getTransactionId().isEmpty()) {
            graphEvent.setTransactionId(UUID.randomUUID().toString());
        }
        if (graphEvent.getRelationship() != null) {
            GraphEventTransformer.validateEdgeModel(graphEvent.getRelationship());
        } else if (graphEvent.getVertex() != null) {
            GraphEventTransformer.validateVertexModel(graphEvent.getVertex());
        } else {
            throw new SpikeException("Unable to parse event: " + event);
        }

        return graphEvent;
    }

    private String extractEventBody(String event) {
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(event);
        return jsonElement.getAsJsonObject().get("body").toString();
    }
}
