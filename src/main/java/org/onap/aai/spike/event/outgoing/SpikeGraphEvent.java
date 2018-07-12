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
package org.onap.aai.spike.event.outgoing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.onap.aai.spike.exception.SpikeException;

public class SpikeGraphEvent {

    public enum SpikeOperation {
        CREATE, UPDATE, DELETE
    }

    private SpikeOperation operation;

    @SerializedName("transaction-id")
    private String transactionId;

    @SerializedName("database-transaction-id")
    private String dbTransactionId;

    @SerializedName("timestamp")
    private long operationTimestamp;

    private SpikeVertex vertex;

    private SpikeEdge relationship;

    // Time this event was received in spike, used to determine when to send the event
    @GsonExclude
    private long spikeTimestamp = System.currentTimeMillis();

    /** Serializer/deserializer for converting to/from JSON. */
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public SpikeOperation getOperation() {
        return operation;
    }

    public void setOperation(SpikeOperation operation) {
        this.operation = operation;
    }

    public long getOperationTimestamp() {
        return operationTimestamp;
    }

    public void setOperationTimestamp(long operationTimestamp) {
        this.operationTimestamp = operationTimestamp;
    }



    public SpikeVertex getVertex() {
        return vertex;
    }

    public void setVertex(SpikeVertex vertex) {
        this.vertex = vertex;
    }

    public SpikeEdge getRelationship() {
        return relationship;
    }

    public void setRelationship(SpikeEdge relationship) {
        this.relationship = relationship;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setDbTransactionId(String dbTransactionId) {
        this.dbTransactionId = dbTransactionId;
    }

    public long getSpikeTimestamp() {
        return spikeTimestamp;
    }

    /**
     * Unmarshalls this Vertex object into a JSON string.
     *
     * @return - A JSON format string representation of this Vertex.
     */
    public String toJson() {
        return gson.toJson(this);
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
    public static SpikeGraphEvent fromJson(String json) throws SpikeException {

        try {
            // Make sure that we were actually provided a non-empty string
            // before we
            // go any further.
            if (json == null || json.isEmpty()) {
                throw new SpikeException("Empty or null JSON string.");
            }

            // Marshall the string into a Vertex object.
            return gson.fromJson(json, SpikeGraphEvent.class);

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

