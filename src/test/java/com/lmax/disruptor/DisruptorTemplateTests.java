package com.lmax.disruptor;

import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.DisruptorEventFactory;
import com.lmax.disruptor.event.translator.DisruptorEventOneArgTranslator;
import com.lmax.disruptor.thread.DisruptorThreadFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DisruptorTemplateTests {

    private Disruptor<DisruptorEvent> disruptor;
    private DisruptorTemplate template;

    @BeforeEach
    void setUp() {
        disruptor = new Disruptor<>(
                new DisruptorEventFactory(),
                16,
                DisruptorThreadFactory.DEFAULT_THREAD_FACTORY);
        template = new DisruptorTemplate(disruptor, new DisruptorEventOneArgTranslator());
    }

    @AfterEach
    void tearDown() {
        disruptor.shutdown();
    }

    @Test
    void shouldCreateTemplate() {
        assertNotNull(template);
    }

    @Test
    void shouldPublishEventObject() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        DisruptorEventHandler handler = new DisruptorEventHandler(latch);
        disruptor.handleEventsWith(handler);
        disruptor.start();

        DisruptorEvent event = new DisruptorEvent();
        event.setTopic("test");
        event.setTag("tag");
        template.publishEvent(event);

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("test", handler.receivedEvent.getTopic());
    }

    @Test
    void shouldPublishEventWithTopicTagPayload() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        DisruptorEventHandler handler = new DisruptorEventHandler(latch);
        disruptor.handleEventsWith(handler);
        disruptor.start();

        template.publishEvent("myTopic", "myTag", "payload");

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("myTopic", handler.receivedEvent.getTopic());
        assertEquals("myTag", handler.receivedEvent.getTag());
        assertEquals("payload", handler.receivedEvent.getPayload());
    }

    @Test
    void shouldPublishEventWithAllFields() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        DisruptorEventHandler handler = new DisruptorEventHandler(latch);
        disruptor.handleEventsWith(handler);
        disruptor.start();

        template.publishEvent("topic", "ns", "tag", "data");

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("topic", handler.receivedEvent.getTopic());
        assertEquals("ns", handler.receivedEvent.getNamespace());
        assertEquals("tag", handler.receivedEvent.getTag());
        assertEquals("data", handler.receivedEvent.getPayload());
        assertNotNull(handler.receivedEvent.getMessageId());
    }

    private static class DisruptorEventHandler implements EventHandler<DisruptorEvent> {
        final CountDownLatch latch;
        DisruptorEvent receivedEvent;

        DisruptorEventHandler(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onEvent(DisruptorEvent event, long sequence, boolean endOfBatch) {
            this.receivedEvent = event;
            latch.countDown();
        }
    }
}
