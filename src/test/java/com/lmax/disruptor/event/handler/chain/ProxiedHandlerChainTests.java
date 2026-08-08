package com.lmax.disruptor.event.handler.chain;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.DisruptorHandler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ProxiedHandlerChainTests {

    @Test
    void shouldCreateEmptyChain() {
        ProxiedHandlerChain chain = new ProxiedHandlerChain();
        assertNotNull(chain);
    }

    @Test
    void shouldDoNothingOnEmptyChain() throws Exception {
        ProxiedHandlerChain chain = new ProxiedHandlerChain();
        DisruptorEvent event = new DisruptorEvent();
        // Should not throw
        chain.doHandler(event);
    }

    @Test
    void shouldExecuteHandlersInOrder() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        List<Integer> order = new ArrayList<>();

        List<DisruptorHandler<DisruptorEvent>> handlers = new ArrayList<>();
        handlers.add((event, chain) -> {
            order.add(counter.getAndIncrement());
            chain.doHandler(event);
        });
        handlers.add((event, chain) -> {
            order.add(counter.getAndIncrement());
            chain.doHandler(event);
        });

        ProxiedHandlerChain original = new ProxiedHandlerChain();
        ProxiedHandlerChain chain = new ProxiedHandlerChain(original, handlers);
        chain.doHandler(new DisruptorEvent());

        assertEquals(2, order.size());
        assertEquals(0, order.get(0));
        assertEquals(1, order.get(1));
    }

    @Test
    void shouldDelegateToOriginalChain() throws Exception {
        AtomicInteger originalCalled = new AtomicInteger(0);

        ProxiedHandlerChain original = new ProxiedHandlerChain() {
            @Override
            public void doHandler(DisruptorEvent event) {
                originalCalled.incrementAndGet();
            }
        };

        List<DisruptorHandler<DisruptorEvent>> handlers = new ArrayList<>();
        handlers.add((event, chain) -> chain.doHandler(event));

        ProxiedHandlerChain chain = new ProxiedHandlerChain(original, handlers);
        chain.doHandler(new DisruptorEvent());

        assertEquals(1, originalCalled.get());
    }

    @Test
    void shouldThrowOnNullOriginalChain() {
        assertThrows(NullPointerException.class, () ->
                new ProxiedHandlerChain(null, new ArrayList<>()));
    }
}
