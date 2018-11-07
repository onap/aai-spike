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
package org.onap.aai.spike.service;

import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.PriorityBlockingQueue;
import javax.naming.OperationNotSupportedException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.event.api.EventConsumer;
import org.onap.aai.event.api.EventPublisher;
import org.onap.aai.event.api.MessageWithOffset;
import org.onap.aai.spike.event.envelope.EventEnvelope;
import org.onap.aai.spike.event.envelope.EventEnvelopeParser;
import org.onap.aai.spike.event.incoming.GizmoGraphEvent;
import org.onap.aai.spike.event.incoming.OffsetManager;
import org.onap.aai.spike.event.outgoing.SpikeEventComparator;
import org.onap.aai.spike.event.outgoing.SpikeEventExclusionStrategy;
import org.onap.aai.spike.event.outgoing.SpikeGraphEvent;
import org.onap.aai.spike.exception.SpikeException;
import org.onap.aai.spike.logging.SpikeMsgs;
import org.onap.aai.spike.util.SpikeConstants;
import org.onap.aai.spike.util.SpikeProperties;

public class SpikeEventProcessor extends TimerTask {

    /**
     * Client used for consuming events to the event bus.
     */
    private EventConsumer consumer;
    /**
     * Client used for publishing events to the event bus.
     */
    private EventPublisher publisher;
    /**
     * Internal queue where outgoing events will be buffered until they can be serviced by the event
     * publisher worker threads.
     */
    private BlockingQueue<SpikeGraphEvent> eventQueue;

    private Integer eventQueueCapacity = DEFAULT_EVENT_QUEUE_CAPACITY;
    private Integer eventOffsetPeriod = DEFAULT_EVENT_OFFSET_COMMIT_PERIOD;

    private OffsetManager offsetManager;
    private Long lastCommittedOffset = null;
    private EventEnvelopeParser eventEnvelopeParser;

    /**
     * Number of events that can be queued up for publishing before it is dropped
     */
    private static final Integer DEFAULT_EVENT_QUEUE_CAPACITY = 10000;
    private static final Integer DEFAULT_EVENT_OFFSET_COMMIT_PERIOD = 10000;

    private static Logger logger = LoggerFactory.getInstance().getLogger(SpikeEventProcessor.class.getName());
    private static Logger auditLogger = LoggerFactory.getInstance().getAuditLogger(SpikeEventProcessor.class.getName());
    private static final Gson gson =
            new GsonBuilder().setExclusionStrategies(new SpikeEventExclusionStrategy()).setPrettyPrinting().create();

    public SpikeEventProcessor(EventConsumer consumer, EventPublisher publisher) {
        this.consumer = consumer;
        this.publisher = publisher;

        try {
            eventQueueCapacity = Integer.parseInt(SpikeProperties.get(SpikeConstants.SPIKE_EVENT_QUEUE_CAPACITY));
            eventOffsetPeriod = Integer.parseInt(SpikeProperties.get(SpikeConstants.SPIKE_EVENT_OFFSET_CHECK_PERIOD));

        } catch (Exception ex) {
        }

        eventQueue = new PriorityBlockingQueue<SpikeGraphEvent>(eventQueueCapacity, new SpikeEventComparator());
        new Thread(new SpikeEventPublisher()).start();

        // Instantiate the offset manager. This will run a background thread that
        // periodically updates the value of the most recent offset value that can
        // be safely committed with the event bus.
        offsetManager = new OffsetManager(eventQueueCapacity, eventOffsetPeriod);
        eventEnvelopeParser = new EventEnvelopeParser();
    }

    @Override
    public void run() {
        logger.info(SpikeMsgs.SPIKE_QUERY_EVENT_SYSTEM);

        if (consumer == null) {
            logger.error(SpikeMsgs.SPIKE_SERVICE_STARTED_FAILURE, SpikeConstants.SPIKE_SERVICE_NAME);
        }

        Iterable<MessageWithOffset> events = null;
        try {
            events = consumer.consumeWithOffsets();

        } catch (OperationNotSupportedException e) {
            // This means we are using DMaaP and can't use offsets
            try {
                Iterable<String> tempEvents = consumer.consume();
                ArrayList<MessageWithOffset> messages = new ArrayList<MessageWithOffset>();
                for (String event : tempEvents) {
                    messages.add(new MessageWithOffset(0, event));
                }
                events = messages;
            } catch (Exception e1) {
                logger.error(SpikeMsgs.SPIKE_EVENT_CONSUME_FAILURE, e1.getMessage());
                return;
            }
        } catch (Exception e) {
            logger.error(SpikeMsgs.SPIKE_EVENT_CONSUME_FAILURE, e.getMessage());
            return;
        }

        if (events == null || !events.iterator().hasNext()) {
            logger.info(SpikeMsgs.SPIKE_NO_EVENT_RECEIVED);
        }

        for (MessageWithOffset event : events) {
            try {
                logger.debug(SpikeMsgs.SPIKE_EVENT_RECEIVED, event.getMessage());

                GizmoGraphEvent modelEvent = eventEnvelopeParser.parseEvent(event.getMessage());
                auditLogger.info(SpikeMsgs.SPIKE_EVENT_RECEIVED,
                        "of type: " + modelEvent.getObjectType() + " with key: " + modelEvent.getObjectKey()
                                + " , transaction-id: " + modelEvent.getTransactionId());
                logger.info(SpikeMsgs.SPIKE_EVENT_RECEIVED, "of type: " + modelEvent.getObjectType() + " with key: "
                        + modelEvent.getObjectKey() + " , transaction-id: " + modelEvent.getTransactionId());

                String modelEventJson = gson.toJson(modelEvent);

                // Log the current event as 'being processed' with the offset manager so that we know that it's
                // associated offset is not yet save to be committed as 'done'.
                offsetManager.cacheEvent(modelEvent.getTransactionId(), event.getOffset());

                while (eventQueue.size() >= eventQueueCapacity) {
                    // Wait until there's room in the queue
                    logger.error(SpikeMsgs.SPIKE_EVENT_PUBLISH_FAILURE,
                            "Event could not be published to the event bus due to: Internal buffer capacity exceeded. Waiting 10 seconds.");
                    Thread.sleep(10000);
                }

                eventQueue.offer(modelEvent.toSpikeGraphEvent());

                logger.info(SpikeMsgs.SPIKE_EVENT_PROCESSED, "of type: " + modelEvent.getObjectType() + " with key: "
                        + modelEvent.getObjectKey() + " , transaction-id: " + modelEvent.getTransactionId());
                logger.debug(SpikeMsgs.SPIKE_EVENT_PROCESSED, modelEventJson);

            } catch (SpikeException | InterruptedException e) {
                logger.error(SpikeMsgs.SPIKE_EVENT_CONSUME_FAILURE,
                        e.getMessage() + ".  Incoming event payload:\n" + event.getMessage());
            } catch (Exception e) {
                logger.error(SpikeMsgs.SPIKE_EVENT_CONSUME_FAILURE,
                        e.getMessage() + ".  Incoming event payload:\n" + event.getMessage());
            }
        }

        try {

            // Get the next 'safe' offset to be committed from the offset manager.
            // We need to do this here istead of letting the offset manager just take care
            // of it for us because the event consumer is not thread safe. If we try to
            // commit the offsets from another thread, it gets unhappy...
            Long nextOffset = offsetManager.getNextOffsetToCommit();

            // Make sure we actually have a real value...
            if (nextOffset != null) {

                // There is no point in continually committing the same offset value, so make sure
                // that something has actually changed before we do anything...
                if ((lastCommittedOffset == null) || (!lastCommittedOffset.equals(nextOffset))) {

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Committing offset: " + nextOffset + " to the event bus for Champ raw event topic.");
                    }

                    // OK, let's commit the latest value...
                    consumer.commitOffsets(nextOffset);
                    lastCommittedOffset = nextOffset;
                }
            }
        } catch (OperationNotSupportedException e) {
            // We must be working with a DMaap which doesn't support offset management. Swallow
            // the exception
        } catch (Exception e) {
            logger.error(SpikeMsgs.SPIKE_EVENT_CONSUME_FAILURE, e.getMessage());
        }
    }

    /**
     * This class implements the threads which is responsible for buffering the events in memory and
     * ordering them before publishing it to topic
     * <p>
     * Each publish operation is performed synchronously, so that the thread will only move on to the
     * next available event once it has actually published the current event to the bus.
     */
    private class SpikeEventPublisher implements Runnable {

        /**
         * Partition key to use when publishing events to the event stream. We WANT all events to go to a
         * single partition, so we are just using a hard-coded key for every event.
         */
        private static final String EVENTS_PARTITION_KEY = "SpikeEventKey";
        private static final int DEFAULT_EVENT_QUEUE_DELAY = 10000;

        Integer eventQueueDelay = DEFAULT_EVENT_QUEUE_DELAY;

        public SpikeEventPublisher() {
            try {
                eventQueueDelay = Integer.parseInt(SpikeProperties.get(SpikeConstants.SPIKE_EVENT_QUEUE_DELAY));
            } catch (Exception ex) {
            }
        }

        @Override
        public void run() {
            while (true) {

                SpikeGraphEvent nextEvent;
                SpikeGraphEvent event = null;
                try {

                    // Get the next event to be published from the queue if it is old enough or we have too
                    // many items in the queue
                    if ((nextEvent = eventQueue.peek()) != null
                            && (System.currentTimeMillis() - nextEvent.getSpikeTimestamp() > eventQueueDelay
                                    || eventQueue.size() > eventQueueCapacity)) {
                        event = eventQueue.take();
                    } else {
                        continue;
                    }

                } catch (InterruptedException e) {

                    // Restore the interrupted status.
                    Thread.currentThread().interrupt();
                }

                // Try publishing the event to the event bus. This call will block
                // until the event is published or times out.
                try {
                    String eventJson = gson.toJson(new EventEnvelope(event));
                    int sentMessageCount = publisher.sendSync(EVENTS_PARTITION_KEY, eventJson);
                    if (sentMessageCount > 0) {
                        logger.info(SpikeMsgs.SPIKE_EVENT_PUBLISHED, "of type: " + event.getObjectType() + " with key: "
                                + event.getObjectKey() + " , transaction-id: " + event.getTransactionId());
                        logger.debug(SpikeMsgs.SPIKE_EVENT_PUBLISHED, eventJson);
                    } else {
                        logger.warn(SpikeMsgs.SPIKE_PUBLISH_FAILED, "of type: " + event.getObjectType() + " with key: "
                                + event.getObjectKey() + " , transaction-id: " + event.getTransactionId());
                        logger.debug(SpikeMsgs.SPIKE_PUBLISH_FAILED, eventJson);
                    }


                    // Inform the offset manager that this event has been published. It's offset
                    // can now, potentially, be safely committed to the event bus so that on a
                    // restart we won't reprocess it.
                    offsetManager.markAsPublished(event.getTransactionId());

                } catch (ExecutionException e) {

                    // Publish timed out, queue it up to retry again. Since this message was pulled from the
                    // top of the queue, it will go back to the top.
                    logger.error(SpikeMsgs.SPIKE_EVENT_PUBLISH_FAILURE, "Retrying in 60 seconds. " + e.getMessage());
                    eventQueue.offer(event);

                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                } catch (Exception e) {
                    logger.error(SpikeMsgs.SPIKE_EVENT_PUBLISH_FAILURE, e.getMessage());
                }
            }
        }
    }

}
