package com.lmax.disruptor.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisruptorEventFactoryTests {

    @Test
    void shouldCreateNewInstance() {
        DisruptorEventFactory factory = new DisruptorEventFactory();
        DisruptorEvent event = factory.newInstance();
        assertNotNull(event);
        assertNotNull(event.getSource());
        assertTrue(event.getTimestamp() > 0);
    }

    @Test
    void shouldCreateDistinctInstances() {
        DisruptorEventFactory factory = new DisruptorEventFactory();
        DisruptorEvent event1 = factory.newInstance();
        DisruptorEvent event2 = factory.newInstance();
        assertNotSame(event1, event2);
    }
}
