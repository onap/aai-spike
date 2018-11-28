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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.spike.OXMModelLoaderSetup;
import org.onap.aai.spike.exception.SpikeException;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EdgeRulesLoaderTest extends OXMModelLoaderSetup {

    @Test
    public void loadModels() throws Exception {
        EdgeRulesLoader.loadModels();
        assertTrue(EdgeRulesLoader.getSchemaForVersion("v11")
                .isValidType("org.onap.relationships.inventory.groupsResourcesIn"));
    }

    @Test
    public void loadModelsWithAVersion() throws Exception {
        EdgeRulesLoader.resetSchemaVersionContext();
        EdgeRulesLoader.loadModels("V11");
        assertEquals(1, EdgeRulesLoader.getSchemas().size());
        assertEquals("v11", EdgeRulesLoader.getLatestSchemaVersion());
    }

    @Test
    public void getSchemaForVersion() throws Exception {
        EdgeRulesLoader.resetSchemaVersionContext();
        EdgeRulesLoader.loadModels("v11");
        String version = EdgeRulesLoader.getLatestSchemaVersion();
        RelationshipSchema schema = EdgeRulesLoader.getSchemaForVersion(version);
        assertNotNull(schema.lookupRelationType("org.onap.relationships.inventory.groupsResourcesIn"));
        assertNotNull(schema.lookupRelation("U:V:org.onap.relationships.inventory.groupsResourcesIn"));
        assertNull(schema.lookupRelation("U:W:org.onap.relationships.inventory.groupsResourcesIn"));
    }


    @Test
    public void getSchemaForVersionFail() throws Exception {
        EdgeRulesLoader.loadModels();
        try {
            EdgeRulesLoader.getSchemaForVersion("v1");
        } catch (SpikeException e) {
            assertEquals("Error loading DB Edge Rules for: v1", e.getMessage());
        }
    }
}
