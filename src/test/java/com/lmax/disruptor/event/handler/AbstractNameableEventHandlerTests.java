package com.lmax.disruptor.event.handler;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.chain.HandlerChain;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractNameableEventHandlerTests {

    @Test
    void shouldSetAndGetName() {
        TestHandler handler = new TestHandler();
        handler.setName("myHandler");
        assertEquals("myHandler", handler.getName());
    }

    @Test
    void shouldHaveNullNameByDefault() {
        TestHandler handler = new TestHandler();
        assertNull(handler.getName());
    }

    private static class TestHandler extends AbstractNameableEventHandler<DisruptorEvent> {
        @Override
        public void doHandler(DisruptorEvent event, HandlerChain<DisruptorEvent> handlerChain) throws Exception {
            handlerChain.doHandler(event);
        }
    }
}
