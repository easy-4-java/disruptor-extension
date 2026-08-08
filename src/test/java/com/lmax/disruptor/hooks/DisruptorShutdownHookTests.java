package com.lmax.disruptor.hooks;

import com.lmax.disruptor.DisruptorTemplate;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.DisruptorEventFactory;
import com.lmax.disruptor.event.translator.DisruptorEventOneArgTranslator;
import com.lmax.disruptor.thread.DisruptorThreadFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisruptorShutdownHookTests {

    @Test
    void shouldCreateShutdownHook() {
        Disruptor<DisruptorEvent> disruptor = new Disruptor<>(
                new DisruptorEventFactory(),
                16,
                DisruptorThreadFactory.DEFAULT_THREAD_FACTORY);
        DisruptorShutdownHook hook = new DisruptorShutdownHook(disruptor);
        assertNotNull(hook);
        assertInstanceOf(Thread.class, hook);
        disruptor.shutdown();
    }

    @Test
    void shouldShutdownDisruptorOnRun() {
        Disruptor<DisruptorEvent> disruptor = new Disruptor<>(
                new DisruptorEventFactory(),
                16,
                DisruptorThreadFactory.DEFAULT_THREAD_FACTORY);
        DisruptorShutdownHook hook = new DisruptorShutdownHook(disruptor);
        hook.run();
        // After shutdown, starting should throw or be a no-op
        // Just verifying no exception was thrown during run
    }
}
