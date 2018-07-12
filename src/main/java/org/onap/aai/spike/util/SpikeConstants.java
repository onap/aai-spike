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
package org.onap.aai.spike.util;

public class SpikeConstants {
    // Logging related
    public static final String SPIKE_SERVICE_NAME = "spike";

    public static final String SPIKE_FILESEP =
            (System.getProperty("file.separator") == null) ? "/" : System.getProperty("file.separator");

    public static final String SPIKE_SPECIFIC_CONFIG = System.getProperty("CONFIG_HOME") + SPIKE_FILESEP;

    public static final String SPIKE_HOME_MODEL = SPIKE_SPECIFIC_CONFIG + "model" + SPIKE_FILESEP;
    public static final String SPIKE_EVENT_POLL_INTERVAL = "spike.event.poll.interval";
    public static final String SPIKE_EVENT_QUEUE_CAPACITY = "spike.event.queue.capacity";
    public static final String SPIKE_EVENT_QUEUE_DELAY = "spike.event.queue.delay";
    public static final String SPIKE_EVENT_OFFSET_CHECK_PERIOD = "spike.event.offset.period";
    public static final String SPIKE_PROPS_RESERVED = "spike.props.reserved";
    public static final String SPIKE_CONFIG_FILE = SPIKE_SPECIFIC_CONFIG + "spike.properties";
}
