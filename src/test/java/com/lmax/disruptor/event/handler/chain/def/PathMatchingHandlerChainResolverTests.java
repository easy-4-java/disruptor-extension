package com.lmax.disruptor.event.handler.chain.def;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.DisruptorHandler;
import com.lmax.disruptor.event.handler.chain.HandlerChain;
import com.lmax.disruptor.event.handler.chain.HandlerChainManager;
import com.lmax.disruptor.event.handler.chain.ProxiedHandlerChain;
import com.lmax.disruptor.util.AntPathMatcher;
import com.lmax.disruptor.util.PathMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathMatchingHandlerChainResolverTests {

    private PathMatchingHandlerChainResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new PathMatchingHandlerChainResolver();
    }

    @Test
    void shouldCreateWithDefaults() {
        assertNotNull(resolver.getHandlerChainManager());
        assertNotNull(resolver.getPathMatcher());
    }

    @Test
    void shouldReturnNullWhenNoChains() {
        DisruptorEvent event = new DisruptorEvent();
        HandlerChain<DisruptorEvent> original = new ProxiedHandlerChain();
        assertNull(resolver.getChain(event, original));
    }

    @Test
    void shouldResolveMatchingChain() {
        HandlerChainManager<DisruptorEvent> manager = resolver.getHandlerChainManager();
        DisruptorHandler<DisruptorEvent> handler = (event, chain) -> {};
        manager.addHandler("h1", handler);
        manager.createChain("/topic/**", "h1");

        DisruptorEvent event = new DisruptorEvent();
        event.setTopic("topic");
        event.setTag("anything");

        HandlerChain<DisruptorEvent> original = new ProxiedHandlerChain();
        HandlerChain<DisruptorEvent> resolved = resolver.getChain(event, original);
        assertNotNull(resolved);
    }

    @Test
    void shouldReturnNullForNonMatchingChain() {
        HandlerChainManager<DisruptorEvent> manager = resolver.getHandlerChainManager();
        DisruptorHandler<DisruptorEvent> handler = (event, chain) -> {};
        manager.addHandler("h1", handler);
        manager.createChain("/other/**", "h1");

        DisruptorEvent event = new DisruptorEvent();
        event.setTopic("topic");

        HandlerChain<DisruptorEvent> original = new ProxiedHandlerChain();
        assertNull(resolver.getChain(event, original));
    }

    @Test
    void shouldSetHandlerChainManager() {
        DefaultHandlerChainManager newManager = new DefaultHandlerChainManager();
        resolver.setHandlerChainManager(newManager);
        assertSame(newManager, resolver.getHandlerChainManager());
    }

    @Test
    void shouldSetPathMatcher() {
        AntPathMatcher matcher = new AntPathMatcher();
        resolver.setPathMatcher(matcher);
        assertSame(matcher, resolver.getPathMatcher());
    }
}
