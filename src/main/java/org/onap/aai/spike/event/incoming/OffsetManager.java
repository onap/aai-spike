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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.spike.exception.SpikeException;
import org.onap.aai.spike.logging.SpikeMsgs;


/**
 * Instances of this class maintain a buffer of events which have been received and are queued up to
 * be processed.
 * <p>
 * A background thread advances a pointer into the buffer which always points to the head of the
 * most recent consecutive block of processed events. This allows us, at any time, to know what
 * offset value can be safely committed to the event store (meaning any events before that offset
 * into the event topic will not be reprocessed on a restart).
 */
public class OffsetManager {

    /** Buffer that we use for caching 'in flight' events. */
    private RingEntry[] ringBuffer;

    /** Number of elements that can be stored in the buffer. */
    private int bufferSize;

    /** Pointer to the next free slot in the buffer. */
    private AtomicLong writePointer = new AtomicLong(0L);

    /**
     * Pointer to the next slot in the buffer to wait to be published so that we can commit its offset.
     */
    private long commitPointer = 0;

    /**
     * Executor for scheduling the background task which commits offsets to the event bus.
     */
    private ScheduledExecutorService offsetCommitService = Executors.newScheduledThreadPool(1);

    /**
     * The next offset value which represents the head of a consecutive block of events which have been
     * processed.
     */
    private Long nextOffsetToCommit = null;

    private static Logger logger = LoggerFactory.getInstance().getLogger(OffsetManager.class.getName());


    /**
     * Creates a new instance of the offset manager.
     * 
     * @param bufferCapacity - The requested size of the buffer that we will use to cache offsets for
     *        events that are waiting to be processed.
     * @param offsetCheckPeriodMs - The period at which we will try to update what we consider to be the
     *        next offset that can be safely committed to the event bus.
     */
    public OffsetManager(int bufferCapacity, long offsetCheckPeriodMs) {

        // In order to make the math work nicely for our write and commit pointers, we
        // need our buffer size to be a power of 2, so round the supplied buffer size
        // up to ensure that it is a power of two.
        //
        // This way we can just keep incrementing our pointers forever without worrying
        // about wrapping (we'll eventually roll over from LongMax to LongMin, but if the
        // buffer size is a power of 2 then our modded physical indexes will still magically
        // map to the next consecutive index. (Math!)
        bufferSize = nextPowerOf2(bufferCapacity);

        // Now, allocate and initialize our ring buffer.
        ringBuffer = new RingEntry[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            ringBuffer[i] = new RingEntry();
        }

        // Schedule a task to commit the most recent offset value to the event library.
        offsetCommitService.scheduleAtFixedRate(new OffsetCommitter(), offsetCheckPeriodMs, offsetCheckPeriodMs,
                TimeUnit.MILLISECONDS);

        logger.info(SpikeMsgs.OFFSET_MANAGER_STARTED, Integer.toString(bufferSize), Long.toString(offsetCheckPeriodMs));
    }


    /**
     * Logs an event with the offset manager.
     * 
     * @param transactionId - The transaction id associated with this event.
     * @param commitOffset - The event bus offset associated with this event.
     * 
     * @return - The index into the offset manager's buffer for this event.
     */
    public int cacheEvent(String transactionId, long commitOffset) {

        // Get the index to the next free slot in the ring...
        int index = nextFreeSlot();

        if (logger.isDebugEnabled()) {
            logger.debug("Caching event with transaction-id: " + transactionId + " offset: " + commitOffset
                    + " to offset manager at index: " + index);
        }

        // ...and update it with the event meta data we want to cache.
        ringBuffer[index].setTransactionId(transactionId);
        ringBuffer[index].setCommitOffset(commitOffset);

        return index;
    }


    /**
     * Marks a cached event as 'published'.
     * 
     * @param anIndex - The index into the event cache that we want to update.
     * @throws SpikeException
     */
    public void markAsPublished(int anIndex) throws SpikeException {

        // Make sure that we were supplied a valid index.
        if ((anIndex < 0) || (anIndex > bufferSize - 1)) {
            throw new SpikeException("Invalid index " + anIndex + " for offset manager buffer.");
        }

        // It is only valid to mark a cell as 'Published' if it is already
        // in the 'Processing' state.
        if (!ringBuffer[anIndex].state.compareAndSet(RingEntry.PROCESSING, RingEntry.PUBLISHED)) {
            throw new SpikeException("Unexpected event state: " + state2String(ringBuffer[anIndex].state.get()));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Event in offset manger buffer at index: " + anIndex + " marked as 'published'");
        }
    }


    /**
     * Marks a cached event as 'published'.
     * 
     * @param transactionId - The transaction id of the event we want to update.
     * 
     * @throws SpikeException
     */
    public void markAsPublished(String transactionId) throws SpikeException {

        // Iterate over the ring buffer and try to find the specified transaction
        // id.
        for (int i = 0; i < bufferSize; i++) {

            // Is this the one?
            if (ringBuffer[i].getTransactionId() == transactionId) {

                // Found the one we are looking for!
                markAsPublished(i);
                return;
            }
        }

        // If we made it here then we didn't find an event with the supplied transaction id.
        throw new SpikeException("No event with transaction id: " + transactionId + " exists in offset manager buffer");
    }


    /**
     * Retrieves our current view of what is the most recent offset value that can be safely committed
     * to the event bus (meaning that all events on the topic before that offset value have been
     * processed and shouldn't be re-consumed after a restart).
     * 
     * @return - The next 'safe' offset.
     */
    public Long getNextOffsetToCommit() {
        return nextOffsetToCommit;
    }


    /**
     * Finds the next slot in the ring which is marked as 'free'.
     * 
     * @return - An index into the ring buffer.
     */
    private int nextFreeSlot() {

        int currentIndex = (int) (writePointer.getAndIncrement() % bufferSize);
        while (!ringBuffer[currentIndex].state.compareAndSet(RingEntry.FREE, RingEntry.PROCESSING)) {
            currentIndex = (int) (writePointer.getAndIncrement() % bufferSize);
        }

        return currentIndex;
    }


    /**
     * Given a number, this helper method finds the next largest number that is a power of 2.
     * 
     * @param aNumber - The number to compute the next power of two for.
     * 
     * @return - The next largest power of 2 for the supplied number.
     */
    private int nextPowerOf2(int aNumber) {

        int powerOfTwo = 1;
        while (powerOfTwo < aNumber) {
            powerOfTwo *= 2;
        }
        return powerOfTwo;
    }

    private String state2String(int aState) {

        switch (aState) {
            case RingEntry.FREE:
                return "FREE";

            case RingEntry.PROCESSING:
                return "PROCESSING";

            case RingEntry.PUBLISHED:
                return "PUBLISHED";

            default:
                return "UNDEFINED(" + aState + ")";
        }
    }


    /**
     * Defines the structure of the entries in the ring buffer which represent events which are 'in
     * flight'.
     */
    public class RingEntry {

        private final static int FREE = 1; // Slot in buffer is available to be written to.
        private final static int PROCESSING = 2; // Slot in buffer represents an event which is waiting to be processed.
        private final static int PUBLISHED = 3; // Slot in buffer represents an event which has been published.

        /**
         * Describes the state of this entry in the ring:
         * <p>
         * FREE = This slot is currently unused and may be written to.
         * <p>
         * PROCESSING = This slot describes an event which has not yet been published.
         * <p>
         * PUBLISHED = This lot describes an event which has been published and therefore may be released.
         */
        public AtomicInteger state = new AtomicInteger(FREE);

        /** The unique identifier of the event which this entry represents. */
        private String transactionId;

        /** The event bus offset associated with the event which this entry represents. */
        private long commitOffset;


        /**
         * Retrieve the transaction id for the event represented by this entry.
         * 
         * @return - Transaction id.
         */
        public String getTransactionId() {
            return transactionId;
        }


        /**
         * Assigns a transaction id to this entry.
         * 
         * @param transactionId - The unique id for this entry.
         */
        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }


        /**
         * Retrieves the offset of the event represented by this entry.
         * 
         * @return - An event bus offset value.
         */
        public long getCommitOffset() {
            return commitOffset;
        }


        /**
         * Assigns an offset value to this entry.
         * 
         * @param commitOffset - Offset value for this entry.
         */
        public void setCommitOffset(long commitOffset) {
            this.commitOffset = commitOffset;
        }
    }


    /**
     * This class implements a simple background task which wakes up periodically and determines the
     * next available offset from the ring buffer which is safe to commit to the event bus.
     */
    private class OffsetCommitter implements Runnable {

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {

            // Get the index into the ring buffer of the next slot to be checked.
            int currentCommitIndex = (int) (commitPointer % bufferSize);

            // If this entry is in the 'published' state then its offset is good to be
            // committed.
            while (ringBuffer[currentCommitIndex].state.get() == RingEntry.PUBLISHED) {

                // Grab the offset of the current entry.
                nextOffsetToCommit = ringBuffer[currentCommitIndex].getCommitOffset();

                // We don't need to keep the current entry alive any longer, so free it and advance
                // to the next entry in the ring.
                ringBuffer[currentCommitIndex].state.set(RingEntry.FREE);
                commitPointer++;

                // Update our index and loop back to check the next one. We will keep advancing
                // as long as we have consecutive entries that are flagged as 'published'.
                currentCommitIndex = (int) (commitPointer % bufferSize);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Offset to commit to event bus: "
                        + ((nextOffsetToCommit != null) ? nextOffsetToCommit : "none"));
            }
        }
    }
}
