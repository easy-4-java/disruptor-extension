package com.lmax.disruptor.event.factory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisruptorEventLoggerThreadFactoryTests {

    @Test
    void shouldCreateThreadWithExceptionHandler() {
        DisruptorEventLoggerThreadFactory factory = new DisruptorEventLoggerThreadFactory();
        Thread thread = factory.newThread(() -> {});
        assertNotNull(thread);
        assertNotNull(thread.getUncaughtExceptionHandler());
    }

    @Test
    void shouldThrowOnNullRunnable() {
        DisruptorEventLoggerThreadFactory factory = new DisruptorEventLoggerThreadFactory();
        assertThrows(NullPointerException.class, () -> factory.newThread(null));
    }
}
