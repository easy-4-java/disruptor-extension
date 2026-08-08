package com.lmax.disruptor.event.factory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisruptorEventThreadFactoryTests {

    @Test
    void shouldCreateStandardThread() {
        DisruptorEventThreadFactory factory = new DisruptorEventThreadFactory();
        Thread thread = factory.newThread(() -> {});
        assertNotNull(thread);
        assertFalse(thread.isDaemon());
        assertEquals(Thread.NORM_PRIORITY, thread.getPriority());
    }

    @Test
    void shouldThrowOnNullRunnable() {
        DisruptorEventThreadFactory factory = new DisruptorEventThreadFactory();
        assertThrows(NullPointerException.class, () -> factory.newThread(null));
    }
}
