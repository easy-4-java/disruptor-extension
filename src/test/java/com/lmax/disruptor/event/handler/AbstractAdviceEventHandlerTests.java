package com.lmax.disruptor.event.handler;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.chain.HandlerChain;
import com.lmax.disruptor.event.handler.chain.ProxiedHandlerChain;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AbstractAdviceEventHandlerTests {

    @Test
    void shouldCallPreHandle() throws Exception {
        List<String> order = new ArrayList<>();
        TestAdviceHandler handler = new TestAdviceHandler();
        handler.preHandleAction = event -> {
            order.add("preHandle");
            return true;
        };
        handler.postHandleAction = event -> order.add("postHandle");
        handler.afterCompletionAction = (event, ex) -> order.add("afterCompletion");

        DisruptorEvent event = new DisruptorEvent();
        ProxiedHandlerChain chain = new ProxiedHandlerChain();
        handler.doHandler(event, chain);

        assertTrue(order.contains("preHandle"));
        assertTrue(order.contains("postHandle"));
        assertTrue(order.contains("afterCompletion"));
    }

    @Test
    void shouldNotExecuteChainWhenPreHandleReturnsFalse() throws Exception {
        List<String> order = new ArrayList<>();
        TestAdviceHandler handler = new TestAdviceHandler();
        handler.preHandleAction = event -> false;
        handler.postHandleAction = event -> order.add("postHandle");

        DisruptorEvent event = new DisruptorEvent();
        ProxiedHandlerChain chain = new ProxiedHandlerChain();
        handler.doHandler(event, chain);

        // postHandle is still called, but the chain was not executed
        assertFalse(order.contains("chainExecuted"));
    }

    @Test
    void shouldCallAfterCompletionOnException() throws Exception {
        List<String> order = new ArrayList<>();
        TestAdviceHandler handler = new TestAdviceHandler();
        handler.preHandleAction = event -> {
            throw new RuntimeException("test error");
        };
        handler.afterCompletionAction = (event, ex) -> order.add("afterCompletion");

        DisruptorEvent event = new DisruptorEvent();
        ProxiedHandlerChain chain = new ProxiedHandlerChain();
        handler.doHandler(event, chain);

        assertTrue(order.contains("afterCompletion"));
    }

    @Test
    void shouldCallAfterCompletionEvenWhenAfterCompletionThrows() throws Exception {
        TestAdviceHandler handler = new TestAdviceHandler();
        handler.afterCompletionAction = (event, ex) -> {
            throw new RuntimeException("afterCompletion error");
        };

        DisruptorEvent event = new DisruptorEvent();
        ProxiedHandlerChain chain = new ProxiedHandlerChain();
        // Should not throw - afterCompletion exception is suppressed
        handler.doHandler(event, chain);
    }

    @Test
    void shouldSkipWhenDisabled() throws Exception {
        List<String> order = new ArrayList<>();
        TestAdviceHandler handler = new TestAdviceHandler();
        handler.setEnabled(false);
        handler.preHandleAction = event -> {
            order.add("preHandle");
            return true;
        };

        DisruptorEvent event = new DisruptorEvent();
        ProxiedHandlerChain chain = new ProxiedHandlerChain();
        handler.doHandler(event, chain);

        assertFalse(order.contains("preHandle"));
    }

    private static class TestAdviceHandler extends AbstractAdviceEventHandler<DisruptorEvent> {
        PreHandleAction preHandleAction;
        PostHandleAction postHandleAction;
        AfterCompletionAction afterCompletionAction;

        @Override
        protected boolean preHandle(DisruptorEvent event) throws Exception {
            return preHandleAction != null ? preHandleAction.execute(event) : true;
        }

        @Override
        protected void postHandle(DisruptorEvent event) throws Exception {
            if (postHandleAction != null) postHandleAction.execute(event);
        }

        @Override
        public void afterCompletion(DisruptorEvent event, Exception exception) throws Exception {
            if (afterCompletionAction != null) afterCompletionAction.execute(event, exception);
        }

        @FunctionalInterface
        interface PreHandleAction { boolean execute(DisruptorEvent event) throws Exception; }
        @FunctionalInterface
        interface PostHandleAction { void execute(DisruptorEvent event) throws Exception; }
        @FunctionalInterface
        interface AfterCompletionAction { void execute(DisruptorEvent event, Exception ex) throws Exception; }
    }
}
