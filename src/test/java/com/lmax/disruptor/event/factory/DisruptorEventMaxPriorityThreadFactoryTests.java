package com.lmax.disruptor.event.factory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisruptorEventMaxPriorityThreadFactoryTests {

    @Test
    void shouldCreateMaxPriorityThread() {
        DisruptorEventMaxPriorityThreadFactory factory = new DisruptorEventMaxPriorityThreadFactory();
        Thread thread = factory.newThread(() -> {});
        assertNotNull(thread);
        assertEquals(Thread.MAX_PRIORITY, thread.getPriority());
    }

    @Test
    void shouldThrowOnNullRunnable() {
        DisruptorEventMaxPriorityThreadFactory factory = new DisruptorEventMaxPriorityThreadFactory();
        assertThrows(NullPointerException.class, () -> factory.newThread(null));
    }
}
