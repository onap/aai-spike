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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.spike.OXMModelLoaderSetup;
import org.onap.aai.spike.event.incoming.GizmoGraphEvent;
import org.onap.aai.spike.test.util.TestFileReader;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EventEnvelopeParserTest extends OXMModelLoaderSetup {

    @Test
    public void testParseEvent() throws Exception {
        String champNotification = TestFileReader.getFileAsString("event/champ-update-notification-raw.json");
        String expectedParsedEvent = TestFileReader.getFileAsString("event/gizmo-graph-event.json");

        GizmoGraphEvent gizmoGraphEvent = new EventEnvelopeParser().parseEvent(champNotification);

        JSONAssert.assertEquals(expectedParsedEvent, gizmoGraphEvent.toJson(), JSONCompareMode.NON_EXTENSIBLE);
    }
}
