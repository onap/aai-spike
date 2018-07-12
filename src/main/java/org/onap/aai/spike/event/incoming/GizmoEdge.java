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
package org.onap.aai.spike.event.incoming;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import org.onap.aai.spike.exception.SpikeException;

/**
 * This class provides a generic representation of an Edge as provided by the graph data store.
 *
 */
public class GizmoEdge {

    /**
     * The unique identifier used to identify this edge in the graph data store.
     */
    @SerializedName("key")
    private String id;

    /** Type label assigned to this vertex. */
    private String type;

    /** Source vertex for our edge. */
    private GizmoVertex source;

    /** Target vertex for our edge. */
    private GizmoVertex target;

    /** Map of all of the properties assigned to this vertex. */
    private JsonElement properties;

    /** Marshaller/unmarshaller for converting to/from JSON. */
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GizmoVertex getSource() {
        return source;
    }

    public void setSource(GizmoVertex source) {
        this.source = source;
    }

    public GizmoVertex getTarget() {
        return target;
    }

    public void setTarget(GizmoVertex target) {
        this.target = target;
    }

    public JsonElement getProperties() {
        return properties;
    }

    public void setProperties(JsonElement properties) {
        this.properties = properties;
    }

    /**
     * Unmarshalls this Edge object into a JSON string.
     * 
     * @return - A JSON format string representation of this Edge.
     */
    public String toJson() {
        return gson.toJson(this);
    }

    /**
     * Marshalls the provided JSON string into a Edge object.
     * 
     * @param json - The JSON string to produce the Edge from.
     * 
     * @return - A Edge object.
     * 
     * @throws SpikeException
     */
    public static GizmoEdge fromJson(String json) throws SpikeException {

        try {

            // Make sure that we were actually provided a non-empty string
            // before we
            // go any further.
            if (json == null || json.isEmpty()) {
                throw new SpikeException("Empty or null JSON string.");
            }

            // Marshall the string into an Edge object.
            return gson.fromJson(json, GizmoEdge.class);

        } catch (Exception ex) {
            throw new SpikeException("Unable to parse JSON string: " + ex.getMessage());
        }
    }
}
