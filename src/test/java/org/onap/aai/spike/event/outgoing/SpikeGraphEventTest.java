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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class SpikeGraphEventTest {
    @Test
    public void testGetObjectKey() {
        String objectKey;

        SpikeGraphEvent spikeGraphEvent;

        spikeGraphEvent = new SpikeGraphEvent();
        SpikeVertex vertex = new SpikeVertex();
        vertex.setId("addfff68");
        spikeGraphEvent.setVertex(vertex);
        objectKey = spikeGraphEvent.getObjectKey();
        assertEquals("addfff68", objectKey);

        spikeGraphEvent = new SpikeGraphEvent();
        SpikeEdge relationship = new SpikeEdge();
        relationship.setId("909d");

        spikeGraphEvent.setRelationship(relationship);
        objectKey = spikeGraphEvent.getObjectKey();
        assertEquals("909d", objectKey);

        spikeGraphEvent = new SpikeGraphEvent();
        objectKey = spikeGraphEvent.getObjectKey();
        assertNull(objectKey);
    }

    @Test
    public void testGetObjectType() {
        String objectType;
        String type = "pserver";
        SpikeGraphEvent spikeGraphEvent;

        spikeGraphEvent = new SpikeGraphEvent();
        SpikeVertex vertex = new SpikeVertex();
        vertex.setType(type);
        spikeGraphEvent.setVertex(vertex);
        objectType = spikeGraphEvent.getObjectType();
        assertEquals("Vertex->" + type, objectType);

        spikeGraphEvent = new SpikeGraphEvent();
        SpikeEdge relationship = new SpikeEdge();
        relationship.setType(type);

        spikeGraphEvent.setRelationship(relationship);
        objectType = spikeGraphEvent.getObjectType();
        assertEquals("Relationship->" + type, objectType);

        spikeGraphEvent = new SpikeGraphEvent();
        objectType = spikeGraphEvent.getObjectType();
        assertNull(objectType);
    }
}
