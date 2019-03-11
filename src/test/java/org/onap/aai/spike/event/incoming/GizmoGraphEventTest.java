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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.spike.OXMModelLoaderSetup;
import org.onap.aai.spike.event.envelope.EventEnvelopeParser;
import org.onap.aai.spike.event.outgoing.SpikeGraphEvent;
import org.onap.aai.spike.exception.SpikeException;
import org.onap.aai.spike.test.util.TestFileReader;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GizmoGraphEventTest extends OXMModelLoaderSetup {

	private static final String SPIKE_EXCEPTION_MESSAGE = "Unable to parse JSON string: Empty or null JSON string.";	
	
	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();
	
    @Test
    public void TestToSpikeGraphEvent() throws SpikeException, IOException, URISyntaxException {
        String champNotification =
                TestFileReader.getFileAsString("event/champ-update-notification-raw-with-relationship.json");

        GizmoGraphEvent gizmoGraphEvent = new EventEnvelopeParser().parseEvent(champNotification);
        SpikeGraphEvent spikeGraphEvent = gizmoGraphEvent.toSpikeGraphEvent();

        assertEquals("b9c7d24a-64a5-4b89-a10a-a89ce58b1caa", spikeGraphEvent.getRelationship().getId());
        assertEquals("537494bd-1e8a-4198-9712-8cefa0f80457", spikeGraphEvent.getRelationship().getSource().getId());
        assertEquals("981c0494-c742-4d75-851c-8194bbbd8a96", spikeGraphEvent.getRelationship().getTarget().getId());
    }

    @Test
    public void TestGetObjectKey() {
        String objectKey;
        GizmoGraphEvent gizmoGraphEvent;

        gizmoGraphEvent = new GizmoGraphEvent();
        GizmoVertex vertex = new GizmoVertex();
        vertex.setId("addfff68");
        gizmoGraphEvent.setVertex(vertex);
        objectKey = gizmoGraphEvent.getObjectKey();
        assertEquals("addfff68", objectKey);

        gizmoGraphEvent = new GizmoGraphEvent();
        GizmoEdge relationship = new GizmoEdge();
        relationship.setSource(vertex);
        relationship.setTarget(vertex);
        relationship.setId("909d");

        gizmoGraphEvent.setRelationship(relationship);
        objectKey = gizmoGraphEvent.getObjectKey();
        assertEquals("909d", objectKey);

        gizmoGraphEvent = new GizmoGraphEvent();
        objectKey = gizmoGraphEvent.getObjectKey();
        assertNull(objectKey);
    }

    @Test
    public void TestGetObjectType() {
        String objectType;
        String type = "pserver";
        GizmoGraphEvent gizmoGraphEvent;

        gizmoGraphEvent = new GizmoGraphEvent();
        GizmoVertex vertex = new GizmoVertex();
        vertex.setType(type);
        gizmoGraphEvent.setVertex(vertex);
        objectType = gizmoGraphEvent.getObjectType();
        assertEquals("Vertex->" + type, objectType);

        gizmoGraphEvent = new GizmoGraphEvent();
        GizmoEdge relationship = new GizmoEdge();
        relationship.setType(type);

        gizmoGraphEvent.setRelationship(relationship);
        objectType = gizmoGraphEvent.getObjectType();
        assertEquals("Relationship->" + type, objectType);

        gizmoGraphEvent = new GizmoGraphEvent();
        objectType = gizmoGraphEvent.getObjectType();
        assertNull(objectType);
    }
    
    @Test
    public void TestGizmoEdgeExceptionEmpty() throws SpikeException {
    	exceptionRule.expect(SpikeException.class);
    	exceptionRule.expectMessage(SPIKE_EXCEPTION_MESSAGE);
    	GizmoEdge.fromJson("");
    }
    
    @Test
    public void TestGizmoEdgeExceptionNull() throws SpikeException {
    	exceptionRule.expect(SpikeException.class);
    	exceptionRule.expectMessage(SPIKE_EXCEPTION_MESSAGE);
    	GizmoEdge.fromJson(null);
    }
    
    @Test
    public void TestGizmoVertexExceptionEmpty() throws SpikeException {
    	exceptionRule.expect(SpikeException.class);
    	exceptionRule.expectMessage(SPIKE_EXCEPTION_MESSAGE);
    	GizmoVertex.fromJson("");
    }
    
    @Test
    public void TestGizmoVertexExceptionNull() throws SpikeException {
    	exceptionRule.expect(SpikeException.class);
    	exceptionRule.expectMessage(SPIKE_EXCEPTION_MESSAGE);
    	GizmoVertex.fromJson(null);
    }
}
