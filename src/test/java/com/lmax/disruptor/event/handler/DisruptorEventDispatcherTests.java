package com.lmax.disruptor.event.handler;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.chain.HandlerChain;
import com.lmax.disruptor.event.handler.chain.HandlerChainResolver;
import com.lmax.disruptor.event.handler.chain.ProxiedHandlerChain;
import com.lmax.disruptor.event.handler.chain.def.DefaultHandlerChainManager;
import com.lmax.disruptor.event.handler.chain.def.PathMatchingHandlerChainResolver;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DisruptorEventDispatcherTests {

    @Test
    void shouldCreateDispatcher() {
        PathMatchingHandlerChainResolver resolver = new PathMatchingHandlerChainResolver();
        DisruptorEventDispatcher dispatcher = new DisruptorEventDispatcher(resolver, 1);
        assertNotNull(dispatcher);
        assertEquals(1, dispatcher.getOrder());
    }

    @Test
    void shouldGetOrder() {
        PathMatchingHandlerChainResolver resolver = new PathMatchingHandlerChainResolver();
        DisruptorEventDispatcher dispatcher = new DisruptorEventDispatcher(resolver, 5);
        assertEquals(5, dispatcher.getOrder());
    }

    @Test
    void shouldDispatchEvent() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);

        DisruptorHandler<DisruptorEvent> handler = new DisruptorHandler<>() {
            @Override
            public void doHandler(DisruptorEvent event, HandlerChain<DisruptorEvent> handlerChain) throws Exception {
                counter.incrementAndGet();
                handlerChain.doHandler(event);
            }
        };

        PathMatchingHandlerChainResolver resolver = new PathMatchingHandlerChainResolver();
        DefaultHandlerChainManager manager = (DefaultHandlerChainManager) resolver.getHandlerChainManager();
        manager.addHandler("counter", handler);
        manager.createChain("/topic/**", "counter");

        DisruptorEventDispatcher dispatcher = new DisruptorEventDispatcher(resolver, 0);
        dispatcher.setEnabled(true);

        DisruptorEvent event = new DisruptorEvent();
        event.setNamespace("");
        event.setTopic("topic");
        event.setTag("tag");

        dispatcher.onEvent(event, 0L, true);

        assertEquals(1, counter.get());
    }

    @Test
    void shouldNotDispatchWhenDisabled() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);

        DisruptorHandler<DisruptorEvent> handler = new DisruptorHandler<>() {
            @Override
            public void doHandler(DisruptorEvent event, HandlerChain<DisruptorEvent> handlerChain) throws Exception {
                counter.incrementAndGet();
                handlerChain.doHandler(event);
            }
        };

        PathMatchingHandlerChainResolver resolver = new PathMatchingHandlerChainResolver();
        DefaultHandlerChainManager manager = (DefaultHandlerChainManager) resolver.getHandlerChainManager();
        manager.addHandler("counter", handler);
        manager.createChain("/topic/**", "counter");

        DisruptorEventDispatcher dispatcher = new DisruptorEventDispatcher(resolver, 0);
        dispatcher.setEnabled(false);

        DisruptorEvent event = new DisruptorEvent();
        event.setTopic("topic");

        dispatcher.onEvent(event, 0L, true);

        assertEquals(0, counter.get());
    }

    @Test
    void shouldHandleEventWithNoMatchingChain() throws Exception {
        PathMatchingHandlerChainResolver resolver = new PathMatchingHandlerChainResolver();
        DisruptorEventDispatcher dispatcher = new DisruptorEventDispatcher(resolver, 0);
        dispatcher.setEnabled(true);

        DisruptorEvent event = new DisruptorEvent();
        event.setTopic("nomatch");

        // Should not throw
        dispatcher.onEvent(event, 0L, true);
    }
}
