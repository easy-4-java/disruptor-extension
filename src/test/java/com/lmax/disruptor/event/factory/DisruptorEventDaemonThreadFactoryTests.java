package com.lmax.disruptor.event.factory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisruptorEventDaemonThreadFactoryTests {

    @Test
    void shouldCreateDaemonThread() {
        DisruptorEventDaemonThreadFactory factory = new DisruptorEventDaemonThreadFactory();
        Thread thread = factory.newThread(() -> {});
        assertNotNull(thread);
        assertTrue(thread.isDaemon());
    }

    @Test
    void shouldThrowOnNullRunnable() {
        DisruptorEventDaemonThreadFactory factory = new DisruptorEventDaemonThreadFactory();
        assertThrows(NullPointerException.class, () -> factory.newThread(null));
    }
}
