package com.lmax.disruptor.event.handler;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.chain.HandlerChain;
import com.lmax.disruptor.event.handler.chain.ProxiedHandlerChain;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class AbstractEnabledEventHandlerTests {

    @Test
    void shouldBeEnabledByDefault() {
        TestEnabledHandler handler = new TestEnabledHandler();
        assertTrue(handler.isEnabled());
    }

    @Test
    void shouldSetEnabled() {
        TestEnabledHandler handler = new TestEnabledHandler();
        handler.setEnabled(false);
        assertFalse(handler.isEnabled());
    }

    @Test
    void shouldCallDoHandlerInternalWhenEnabled() throws Exception {
        AtomicBoolean called = new AtomicBoolean(false);
        TestEnabledHandler handler = new TestEnabledHandler();
        handler.doHandlerInternal = (event, chain) -> {
            called.set(true);
            chain.doHandler(event);
        };

        DisruptorEvent event = new DisruptorEvent();
        ProxiedHandlerChain chain = new ProxiedHandlerChain();
        handler.doHandler(event, chain);
        assertTrue(called.get());
    }

    @Test
    void shouldSkipDoHandlerInternalWhenDisabled() throws Exception {
        AtomicBoolean called = new AtomicBoolean(false);
        TestEnabledHandler handler = new TestEnabledHandler();
        handler.setEnabled(false);
        handler.doHandlerInternal = (event, chain) -> called.set(true);

        DisruptorEvent event = new DisruptorEvent();
        ProxiedHandlerChain chain = new ProxiedHandlerChain();
        handler.doHandler(event, chain);
        assertFalse(called.get());
    }

    private static class TestEnabledHandler extends AbstractEnabledEventHandler<DisruptorEvent> {
        DoHandlerInternalAction doHandlerInternal;

        @Override
        protected void doHandlerInternal(DisruptorEvent event, HandlerChain<DisruptorEvent> handlerChain) throws Exception {
            if (doHandlerInternal != null) {
                doHandlerInternal.execute(event, handlerChain);
            }
        }

        @FunctionalInterface
        interface DoHandlerInternalAction {
            void execute(DisruptorEvent event, HandlerChain<DisruptorEvent> chain) throws Exception;
        }
    }
}
