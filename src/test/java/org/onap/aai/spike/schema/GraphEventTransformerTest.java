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
package org.onap.aai.spike.schema;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.spike.event.incoming.GizmoGraphEvent;
import org.onap.aai.spike.exception.SpikeException;


/**
 * This set of tests validates the ability of the {@link GraphEventTransformer} to produce OXM model
 * compliant representations of generic graph entities.
 */
public class GraphEventTransformerTest {

    /**
     * Performs all setup steps expected to be performed prior to each test.
     */
    @Before
    public void setup() throws Exception {
        // Load the OXM model definitions.
        OXMModelLoader.loadModels();

        // Load the relationship definitions.
        System.setProperty("CONFIG_HOME", "src/test/resources/");
        EdgeRulesLoader.loadModels();
    }


    /**
     * Validates that, given a raw vertex from the graph abstraction layer, we can transform it into a
     * JSON string which corresponds to the OXM model.
     *
     */
    @Test
    public void vertexToJsonTest() throws Exception {
        // Instantiate the vertex that we will apply the translation to.
        String vertexJson = readFileToString(new File("src/test/resources/vertex.json"));
        GizmoGraphEvent graphEvent = GizmoGraphEvent.fromJson(vertexJson);
        graphEvent.getVertex().getProperties().getAsJsonObject().addProperty("invalid-key1", "invalid-key2");


        // Now, validate our raw vertex from OXM model
        GraphEventTransformer.validateVertexModel(graphEvent.getVertex());

        // Validate the marshalled string we got back against our OXM model.
        assertTrue("Object failed to validate against OXM model.",
                graphEvent.getVertex().getProperties().getAsJsonObject().get("invalid-key1") == null);
    }

    @Test
    public void edgeToJsonTest() throws Exception {
        // Instantiate the edge that we will apply the translation to.
        String edgeJson = readFileToString(new File("src/test/resources/edge.json"));
        GizmoGraphEvent graphEvent = GizmoGraphEvent.fromJson(edgeJson);
        graphEvent.getRelationship().getProperties().getAsJsonObject().addProperty("invalid-key1", "invalid-key2");

        // Now, validate our raw edge from relationship model
        GraphEventTransformer.validateEdgeModel(graphEvent.getRelationship());

        // Validate the marshalled string we got back against our relationship model.
        assertTrue("Object failed to validate against OXM model.",
                graphEvent.getRelationship().getProperties().getAsJsonObject().get("invalid-key1") == null);
    }

    /**
     * This helper method reads the contents of a file into a simple string.
     *
     * @param file - The file to be imported.
     * @return - The file contents expressed as a simple string.
     * @throws IOException if there is a problem reading the file
     */
    public static String readFileToString(File file) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }

            return sb.toString().replaceAll("\\s+", "");
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                fail("Unexpected IOException: " + e.getMessage());
            }
        }
    }
}
