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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.spike.OXMModelLoaderSetup;
import org.onap.aai.spike.exception.SpikeException;

@RunWith(MockitoJUnitRunner.Silent.class)
public class OxmModelLoaderTest extends OXMModelLoaderSetup {

    @Test
    public void testLoadingMultipleOxmFiles() throws SpikeException {
        OXMModelLoader.loadModels();

        DynamicJAXBContext jaxbContext = OXMModelLoader.getContextForVersion(OXMModelLoader.getLatestVersion());

        DynamicType pserver = jaxbContext.getDynamicType("Pserver");
        DynamicType genericVnf = jaxbContext.getDynamicType("GenericVnf");

        assertNotNull(pserver);
        assertNotNull(genericVnf);

        DatabaseMapping mapping = pserver.getDescriptor().getMappings().firstElement();
        if (mapping.isAbstractDirectMapping()) {
            DatabaseField field = mapping.getField();
            String keyName = field.getName().substring(0, field.getName().indexOf("/"));
            assertTrue(keyName.equals("hostname"));
        }

        mapping = genericVnf.getDescriptor().getMappings().firstElement();
        if (mapping.isAbstractDirectMapping()) {
            DatabaseField field = mapping.getField();
            String keyName = field.getName().substring(0, field.getName().indexOf("/"));
            assertTrue(keyName.equals("vnf-id"));
        }

    }

}
