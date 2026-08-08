package com.lmax.disruptor.event.handler;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.chain.HandlerChain;
import com.lmax.disruptor.event.handler.chain.HandlerChainResolver;
import com.lmax.disruptor.event.handler.chain.ProxiedHandlerChain;
import com.lmax.disruptor.exception.EventHandleException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class AbstractRouteableEventHandlerTests {

    @Test
    void shouldCreateWithDefaultConstructor() {
        TestRouteableHandler handler = new TestRouteableHandler();
        assertNotNull(handler);
        assertNull(handler.getHandlerChainResolver());
    }

    @Test
    void shouldCreateWithResolver() {
        HandlerChainResolver<DisruptorEvent> resolver = (event, chain) -> null;
        TestRouteableHandler handler = new TestRouteableHandler(resolver);
        assertSame(resolver, handler.getHandlerChainResolver());
    }

    @Test
    void shouldSetHandlerChainResolver() {
        TestRouteableHandler handler = new TestRouteableHandler();
        HandlerChainResolver<DisruptorEvent> resolver = (event, chain) -> null;
        handler.setHandlerChainResolver(resolver);
        assertSame(resolver, handler.getHandlerChainResolver());
    }

    @Test
    void shouldGetDefaultOrder() {
        TestRouteableHandler handler = new TestRouteableHandler();
        assertEquals(0, handler.getOrder());
    }

    @Test
    void shouldExecuteChainWithoutResolver() throws Exception {
        AtomicBoolean called = new AtomicBoolean(false);
        TestRouteableHandler handler = new TestRouteableHandler();

        DisruptorEvent event = new DisruptorEvent();
        ProxiedHandlerChain chain = new ProxiedHandlerChain();
        handler.doHandler(event, chain);

        // No exception means success
    }

    @Test
    void shouldExecuteChainWithResolver() throws Exception {
        AtomicBoolean called = new AtomicBoolean(false);
        HandlerChainResolver<DisruptorEvent> resolver = (event, origChain) -> {
            called.set(true);
            return origChain;
        };
        TestRouteableHandler handler = new TestRouteableHandler(resolver);

        DisruptorEvent event = new DisruptorEvent();
        ProxiedHandlerChain chain = new ProxiedHandlerChain();
        handler.doHandler(event, chain);

        assertTrue(called.get());
    }

    @Test
    void shouldUseOriginalChainWhenResolverReturnsNull() throws Exception {
        HandlerChainResolver<DisruptorEvent> resolver = (event, origChain) -> null;
        TestRouteableHandler handler = new TestRouteableHandler(resolver);

        DisruptorEvent event = new DisruptorEvent();
        ProxiedHandlerChain chain = new ProxiedHandlerChain();
        // Should not throw
        handler.doHandler(event, chain);
    }

    @Test
    void shouldWrapExceptionInEventHandleException() {
        // The exception wrapping happens inside doHandlerInternal of AbstractRouteableEventHandler,
        // but doHandler in AbstractEnabledEventHandler calls doHandlerInternal directly.
        // If the override throws before calling super, it propagates as-is.
        TestRouteableHandler handler = new TestRouteableHandler();
        handler.throwInExecuteChain = true;

        DisruptorEvent event = new DisruptorEvent();
        ProxiedHandlerChain chain = new ProxiedHandlerChain();
        assertThrows(EventHandleException.class, () -> handler.doHandler(event, chain));
    }

    private static class TestRouteableHandler extends AbstractRouteableEventHandler<DisruptorEvent> {
        boolean throwInExecuteChain = false;

        TestRouteableHandler() { super(); }
        TestRouteableHandler(HandlerChainResolver<DisruptorEvent> resolver) { super(resolver); }

        @Override
        protected void executeChain(DisruptorEvent event, HandlerChain<DisruptorEvent> origChain) throws Exception {
            if (throwInExecuteChain) {
                throw new RuntimeException("test error");
            }
            super.executeChain(event, origChain);
        }
    }
}
