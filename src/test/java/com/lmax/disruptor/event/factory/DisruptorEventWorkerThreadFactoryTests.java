package com.lmax.disruptor.event.factory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisruptorEventWorkerThreadFactoryTests {

    @Test
    void shouldCreateNamedThread() {
        DisruptorEventWorkerThreadFactory factory = new DisruptorEventWorkerThreadFactory("worker");
        Thread t1 = factory.newThread(() -> {});
        assertNotNull(t1);
        assertTrue(t1.getName().startsWith("worker-"));
    }

    @Test
    void shouldIncrementCounter() {
        DisruptorEventWorkerThreadFactory factory = new DisruptorEventWorkerThreadFactory("w");
        Thread t1 = factory.newThread(() -> {});
        Thread t2 = factory.newThread(() -> {});
        assertEquals("w-0", t1.getName());
        assertEquals("w-1", t2.getName());
    }

    @Test
    void shouldThrowOnNullRunnable() {
        DisruptorEventWorkerThreadFactory factory = new DisruptorEventWorkerThreadFactory("w");
        assertThrows(NullPointerException.class, () -> factory.newThread(null));
    }
}
