package com.lmax.disruptor.thread;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisruptorThreadFactoryTests {

    @Test
    void shouldCreateDefaultThread() {
        Thread thread = DisruptorThreadFactory.DEFAULT_THREAD_FACTORY.newThread(() -> {});
        assertNotNull(thread);
        assertTrue(thread.isDaemon());
        assertEquals("Disruptor-Thread", thread.getName());
    }

    @Test
    void shouldCreateLoggerThread() {
        Thread thread = DisruptorThreadFactory.LOGGER_THREAD_FACTORY.newThread(() -> {});
        assertNotNull(thread);
        assertTrue(thread.isDaemon());
        assertEquals("Disruptor-Thread", thread.getName());
        assertNotNull(thread.getUncaughtExceptionHandler());
    }

    @Test
    void shouldCreateMaxPriorityThread() {
        Thread thread = DisruptorThreadFactory.MAX_PRIORITY_THREAD_FACTORY.newThread(() -> {});
        assertNotNull(thread);
        assertTrue(thread.isDaemon());
        assertEquals("Disruptor-Max-Priority-Thread", thread.getName());
        assertEquals(Thread.MAX_PRIORITY, thread.getPriority());
    }

    @Test
    void shouldHaveThreeValues() {
        assertEquals(3, DisruptorThreadFactory.values().length);
    }
}
