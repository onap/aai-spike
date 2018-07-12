/**
 * ﻿============LICENSE_START=======================================================
 * Spike
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property.
 * Copyright © 2017 Amdocs
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 * ECOMP and OpenECOMP are trademarks
 * and service marks of AT&T Intellectual Property.
 */
package org.onap.aai.spike.logging;

import com.att.eelf.i18n.EELFResourceManager;
import org.onap.aai.cl.eelf.LogMessageEnum;

public enum SpikeMsgs implements LogMessageEnum {

    /**
     * Unable to parse schema file: {0} due to error : {1}
     *
     * Arguments: {0} = schema file name {1} = error
     */
    INVALID_OXM_FILE,

    /**
     * Invalid OXM dir: {0}
     *
     * Arguments: {0} = Directory.
     */
    INVALID_OXM_DIR,

    /**
     * Unable to commit offset to event bus due to error: {0}
     *
     * Arguments: {0} = Failure cause.
     */
    OFFSET_COMMIT_FAILURE,

    /**
     * Unable to load OXM schema: {0}
     *
     * Arguments: {0} = error
     */
    OXM_LOAD_ERROR,

    /**
     * OXM file change detected: {0}
     *
     * Arguments: {0} = file name
     */
    OXM_FILE_CHANGED,

    /**
     * Successfully loaded schema: {0}
     *
     * Arguments: {0} = oxm filename
     */
    LOADED_OXM_FILE,

    /**
     * Successfully loaded Edge Properties Files: {0}
     *
     * <p>
     * Arguments: {0} = oxm filename
     */
    LOADED_DB_RULE_FILE,

    /**
     * Successfully Started Spike Service Arguments: {0} = Event interface implementation class name
     */
    SPIKE_SERVICE_STARTED_SUCCESSFULLY,

    /**
     * Event bus offset manager started: buffer size={0} commit period={1}
     *
     * Arguments: {0} = Event buffer capacity {1} = Period in ms at which event bus offsets will be
     * committed.
     */
    OFFSET_MANAGER_STARTED,

    /**
     * Unable to initialize : {0}
     *
     * Arguments: {0} = Service name
     */
    SPIKE_SERVICE_STARTED_FAILURE,

    /**
     * Unable to consume event due to : {0}
     *
     * Arguments: {0} = error message
     */
    SPIKE_EVENT_CONSUME_FAILURE,
    /**
     * Unable to publish event due to : {0}
     *
     * Arguments: {0} = error message
     */
    SPIKE_EVENT_PUBLISH_FAILURE,
    /**
     * Event Received : {0}
     *
     * Arguments: {0} = event
     */
    SPIKE_EVENT_RECEIVED,

    /**
     * Event Processed : {0}
     *
     * Arguments: {0} = event
     */
    SPIKE_EVENT_PROCESSED,
    /**
     * Event Published : {0}
     *
     * Arguments: {0} = event
     */
    SPIKE_EVENT_PUBLISHED,
    /**
     * Event failed to publish: {0}
     *
     * Arguments: {0} = event
     */
    SPIKE_PUBLISH_FAILED,
    /**
     * No Event Received
     *
     * Arguments: none
     */
    SPIKE_NO_EVENT_RECEIVED,
    /**
     * Checking for events Arguments: none
     */
    SPIKE_QUERY_EVENT_SYSTEM,

    /**
     * Schema Ingest properties file was not loaded properly
     */
    SPIKE_SCHEMA_INGEST_LOAD_ERROR,

    /**
     * Received request {0} {1} from {2}. Sending response: {3} Arguments: {0} = operation {1} = target
     * URL {2} = source {3} = response code
     */
    PROCESS_REST_REQUEST;

    /**
     * Static initializer to ensure the resource bundles for this class are loaded...
     */
    static {
        EELFResourceManager.loadMessageBundle("logging/SpikeMsgs");
    }
}
