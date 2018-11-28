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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class OffsetManagerTest {

    /**
     * This test validates that as events are cached, and flagged as published, that the 'safe' offset
     * is advanced correctly.
     */
    @Test
    public void testOffsetAdvancement() throws Exception {

        final Long offsetPeriod = 50L; // ms

        // Create an instance of the offset manager.
        OffsetManager offsetManager = new OffsetManager(10, offsetPeriod);

        // Now, cache some events as if we had consumed them and they
        // are in flight.
        final int event1Index = offsetManager.cacheEvent("1", 1);
        final int event2Index = offsetManager.cacheEvent("2", 2);
        final int event3Index = offsetManager.cacheEvent("3", 3);
        final int event4Index = offsetManager.cacheEvent("4", 4);

        // Mark some of them as 'published'
        offsetManager.markAsPublished(event1Index);
        offsetManager.markAsPublished(event2Index);
        offsetManager.markAsPublished(event4Index);

        // Validate that the offset manager reported the expected offset (ie: we can only commit up
        // to event2, event though event4 has been processed, since event3 is still in flight).
        Long nextOffset = waitForOffsetUpdate(null, offsetPeriod, offsetManager);
        assertTrue("Unexpected 'next offset' value.  Expected=2, actual=" + nextOffset, nextOffset == 2);

        // Now, let's mark event3 as 'published'. We should see the next safe offset
        // advance to event4 (since is was previously flagged as 'published').
        offsetManager.markAsPublished(event3Index);

        nextOffset = waitForOffsetUpdate(nextOffset, offsetPeriod, offsetManager);
        assertTrue("Unexpected 'next offset' value.  Expected=4, actual=" + nextOffset, nextOffset == 4);
    }

    private Long waitForOffsetUpdate(Long currentOffset, Long offsetPeriod, OffsetManager offsetManager)
            throws InterruptedException {

        Long newOffset = currentOffset;
        int retries = 3;
        while (currentOffset == newOffset) {

            // Wait long enough for the offset manager to have hopefully kicked it's offset
            // update task.
            Thread.sleep(offsetPeriod);
            newOffset = offsetManager.getNextOffsetToCommit();

            // We might have just missed the update due to timing, so we will retry a
            // few times before giving up...
            retries--;
            if (retries == 0) {
                fail("Safe offset was not updated as expected");
            }
        }
        return newOffset;
    }

}
