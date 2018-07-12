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
import com.google.gson.annotations.SerializedName;
import org.onap.aai.spike.event.outgoing.SpikeEdge;
import org.onap.aai.spike.event.outgoing.SpikeGraphEvent;
import org.onap.aai.spike.event.outgoing.SpikeGraphEvent.SpikeOperation;
import org.onap.aai.spike.event.outgoing.SpikeVertex;
import org.onap.aai.spike.exception.SpikeException;
import org.onap.aai.spike.schema.EdgeRulesLoader;
import org.onap.aai.spike.schema.OXMModelLoader;

public class GizmoGraphEvent {
    private String operation;

    @SerializedName("transaction-id")
    private String transactionId;

    @SerializedName("database-transaction-id")
    private String dbTransactionId;

    private long timestamp;

    private GizmoVertex vertex;

    private GizmoEdge relationship;

    /** Marshaller/unmarshaller for converting to/from JSON. */
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public GizmoVertex getVertex() {
        return vertex;
    }

    public void setVertex(GizmoVertex vertex) {
        this.vertex = vertex;
    }

    public GizmoEdge getRelationship() {
        return relationship;
    }

    public void setRelationship(GizmoEdge relationship) {
        this.relationship = relationship;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getDbTransactionId() {
        return dbTransactionId;
    }

    /**
     * Unmarshalls this Vertex object into a JSON string.
     * 
     * @return - A JSON format string representation of this Vertex.
     */
    public String toJson() {
        return gson.toJson(this);
    }

    public SpikeGraphEvent toSpikeGraphEvent() throws SpikeException {
        SpikeGraphEvent spikeGraphEvent = new SpikeGraphEvent();
        spikeGraphEvent.setTransactionId(this.getTransactionId());
        spikeGraphEvent.setDbTransactionId(this.getDbTransactionId());
        spikeGraphEvent.setOperationTimestamp(this.getTimestamp());
        if (this.getOperation().equalsIgnoreCase("STORE")) {
            spikeGraphEvent.setOperation(SpikeOperation.CREATE);
        } else if (this.getOperation().equalsIgnoreCase("REPLACE")) {
            spikeGraphEvent.setOperation(SpikeOperation.UPDATE);
        } else if (this.getOperation().equalsIgnoreCase("DELETE")) {
            spikeGraphEvent.setOperation(SpikeOperation.DELETE);
        } else {
            throw new SpikeException("Invalid operation in GizmoGraphEvent: " + this.getOperation());
        }
        if (this.getVertex() != null) {
            SpikeVertex spikeVertex = new SpikeVertex();
            spikeVertex.setId(this.getVertex().getId());
            spikeVertex.setType(this.getVertex().getType());
            spikeVertex.setModelVersion(OXMModelLoader.getLatestVersion());
            spikeVertex.setProperties(this.getVertex().getProperties());
            spikeGraphEvent.setVertex(spikeVertex);

        } else if (this.getRelationship() != null) {
            SpikeEdge spikeEdge = new SpikeEdge();
            spikeEdge.setId(this.getRelationship().getId());
            spikeEdge.setModelVersion(EdgeRulesLoader.getLatestSchemaVersion());
            spikeEdge.setType(this.getRelationship().getType());

            SpikeVertex spikeSourceVertex = new SpikeVertex();
            spikeSourceVertex.setId(this.getRelationship().getSource().getId());
            spikeSourceVertex.setType(this.getRelationship().getSource().getType());
            spikeEdge.setSource(spikeSourceVertex);

            SpikeVertex spikeTargetVertex = new SpikeVertex();
            spikeTargetVertex.setId(this.getRelationship().getTarget().getId());
            spikeTargetVertex.setType(this.getRelationship().getTarget().getType());
            spikeEdge.setTarget(spikeTargetVertex);

            spikeEdge.setProperties(this.getRelationship().getProperties());
            spikeGraphEvent.setRelationship(spikeEdge);
        }

        return spikeGraphEvent;

    }

    /**
     * Marshalls the provided JSON string into a Vertex object.
     * 
     * @param json - The JSON string to produce the Vertex from.
     * 
     * @return - A Vertex object.
     * 
     * @throws SpikeException
     */
    public static GizmoGraphEvent fromJson(String json) throws SpikeException {

        try {

            // Make sure that we were actually provided a non-empty string
            // before we
            // go any further.
            if (json == null || json.isEmpty()) {
                throw new SpikeException("Empty or null JSON string.");
            }

            // Marshall the string into a Vertex object.
            return gson.fromJson(json, GizmoGraphEvent.class);

        } catch (Exception ex) {
            throw new SpikeException("Unable to parse JSON string: " + ex.getMessage());
        }
    }

    @Override
    public String toString() {

        return toJson();
    }

    public String getObjectKey() {
        if (this.getVertex() != null) {
            return this.getVertex().getId();
        } else if (this.getRelationship() != null) {
            return this.getRelationship().getId();
        }

        return null;

    }

    public String getObjectType() {
        if (this.getVertex() != null) {
            return "Vertex->" + this.getVertex().getType();
        } else if (this.getRelationship() != null) {
            return "Relationship->" + this.getRelationship().getType();
        }

        return null;

    }
}
