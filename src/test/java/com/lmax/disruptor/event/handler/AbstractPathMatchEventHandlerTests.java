package com.lmax.disruptor.event.handler;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.chain.HandlerChain;
import com.lmax.disruptor.event.handler.chain.ProxiedHandlerChain;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class AbstractPathMatchEventHandlerTests {

    @Test
    void shouldPassThroughWhenNoPathsConfigured() throws Exception {
        AtomicBoolean called = new AtomicBoolean(false);
        TestPathHandler handler = new TestPathHandler();
        handler.onPreHandleAction = event -> {
            called.set(true);
            return true;
        };

        DisruptorEvent event = new DisruptorEvent();
        event.setNamespace("ns");
        event.setTopic("topic");
        ProxiedHandlerChain chain = new ProxiedHandlerChain();
        handler.doHandler(event, chain);

        assertFalse(called.get());
    }

    @Test
    void shouldCallOnPreHandleWhenPathMatches() throws Exception {
        AtomicBoolean called = new AtomicBoolean(false);
        TestPathHandler handler = new TestPathHandler();
        // Route expression is "namespace/topic/tag" (no leading slash)
        handler.processPath("ns/topic/**");
        handler.onPreHandleAction = event -> {
            called.set(true);
            return true;
        };

        DisruptorEvent event = new DisruptorEvent();
        event.setNamespace("ns");
        event.setTopic("topic");
        event.setTag("tag");
        ProxiedHandlerChain chain = new ProxiedHandlerChain();
        handler.doHandler(event, chain);

        assertTrue(called.get());
    }

    @Test
    void shouldNotCallOnPreHandleWhenPathDoesNotMatch() throws Exception {
        AtomicBoolean called = new AtomicBoolean(false);
        TestPathHandler handler = new TestPathHandler();
        handler.processPath("other/topic/**");
        handler.onPreHandleAction = event -> {
            called.set(true);
            return true;
        };

        DisruptorEvent event = new DisruptorEvent();
        event.setNamespace("ns");
        event.setTopic("topic");
        ProxiedHandlerChain chain = new ProxiedHandlerChain();
        handler.doHandler(event, chain);

        assertFalse(called.get());
    }

    @Test
    void shouldReturnPathMatcher() {
        TestPathHandler handler = new TestPathHandler();
        assertNotNull(handler.getPathMatcher());
    }

    @Test
    void shouldSetPathMatcher() {
        TestPathHandler handler = new TestPathHandler();
        com.lmax.disruptor.util.AntPathMatcher matcher = new com.lmax.disruptor.util.AntPathMatcher();
        handler.setPathMatcher(matcher);
        assertSame(matcher, handler.getPathMatcher());
    }

    @Test
    void shouldGetAppliedPaths() {
        TestPathHandler handler = new TestPathHandler();
        handler.processPath("/a/**");
        handler.processPath("/b/**");
        assertEquals(2, handler.getAppliedPaths().size());
    }

    @Test
    void shouldReturnThisFromProcessPath() {
        TestPathHandler handler = new TestPathHandler();
        DisruptorHandler<DisruptorEvent> result = handler.processPath("/test");
        assertSame(handler, result);
    }

    private static class TestPathHandler extends AbstractPathMatchEventHandler<DisruptorEvent> {
        OnPreHandleAction onPreHandleAction;

        @Override
        protected boolean onPreHandle(DisruptorEvent event) throws Exception {
            return onPreHandleAction != null ? onPreHandleAction.execute(event) : true;
        }

        @FunctionalInterface
        interface OnPreHandleAction { boolean execute(DisruptorEvent event) throws Exception; }
    }
}
