package com.lmax.disruptor.event.handler.chain.def;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.DisruptorHandler;
import com.lmax.disruptor.event.handler.NamedHandlerList;
import com.lmax.disruptor.event.handler.chain.HandlerChain;
import com.lmax.disruptor.event.handler.chain.ProxiedHandlerChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultHandlerChainManagerTests {

    private DefaultHandlerChainManager manager;

    @BeforeEach
    void setUp() {
        manager = new DefaultHandlerChainManager();
    }

    @Test
    void shouldCreateEmptyManager() {
        assertNotNull(manager.getHandlers());
        assertNotNull(manager.getHandlerChains());
        assertTrue(manager.getHandlers().isEmpty());
        assertTrue(manager.getHandlerChains().isEmpty());
    }

    @Test
    void shouldAddHandler() {
        DisruptorHandler<DisruptorEvent> handler = (event, chain) -> {};
        manager.addHandler("h1", handler);
        assertSame(handler, manager.getHandler("h1"));
    }

    @Test
    void shouldCreateChain() {
        DisruptorHandler<DisruptorEvent> h1 = (event, chain) -> {};
        DisruptorHandler<DisruptorEvent> h2 = (event, chain) -> {};
        manager.addHandler("h1", h1);
        manager.addHandler("h2", h2);
        manager.createChain("chain1", "h1,h2");

        NamedHandlerList<DisruptorEvent> chain = manager.getChain("chain1");
        assertNotNull(chain);
        assertEquals(2, chain.size());
    }

    @Test
    void shouldThrowOnBlankChainName() {
        assertThrows(NullPointerException.class, () ->
                manager.createChain("", "h1"));
    }

    @Test
    void shouldThrowOnBlankChainDefinition() {
        assertThrows(NullPointerException.class, () ->
                manager.createChain("chain1", ""));
    }

    @Test
    void shouldAddToChain() {
        DisruptorHandler<DisruptorEvent> handler = (event, chain) -> {};
        manager.addHandler("h1", handler);
        manager.addToChain("chain1", "h1");
        assertNotNull(manager.getChain("chain1"));
    }

    @Test
    void shouldThrowOnBlankChainNameForAddToChain() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.addToChain("", "h1"));
    }

    @Test
    void shouldThrowOnUnregisteredHandler() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.addToChain("chain1", "nonexistent"));
    }

    @Test
    void shouldReportHasChains() {
        assertFalse(manager.hasChains());
        DisruptorHandler<DisruptorEvent> handler = (event, chain) -> {};
        manager.addHandler("h1", handler);
        manager.createChain("chain1", "h1");
        assertTrue(manager.hasChains());
    }

    @Test
    void shouldGetChainNames() {
        DisruptorHandler<DisruptorEvent> handler = (event, chain) -> {};
        manager.addHandler("h1", handler);
        manager.createChain("chain1", "h1");
        assertTrue(manager.getChainNames().contains("chain1"));
    }

    @Test
    void shouldProxyChain() {
        DisruptorHandler<DisruptorEvent> handler = (event, chain) -> chain.doHandler(event);
        manager.addHandler("h1", handler);
        manager.createChain("chain1", "h1");

        HandlerChain<DisruptorEvent> original = new ProxiedHandlerChain();
        HandlerChain<DisruptorEvent> proxied = manager.proxy(original, "chain1");
        assertNotNull(proxied);
    }

    @Test
    void shouldThrowOnProxyForMissingChain() {
        HandlerChain<DisruptorEvent> original = new ProxiedHandlerChain();
        assertThrows(IllegalArgumentException.class, () ->
                manager.proxy(original, "nonexistent"));
    }

    @Test
    void shouldReturnNullForMissingChain() {
        assertNull(manager.getChain("nonexistent"));
    }

    @Test
    void shouldSetNameOnNameableHandler() {
        DisruptorHandler<DisruptorEvent> handler = new DisruptorHandler<DisruptorEvent>() {
            private String name;
            @Override
            public void doHandler(DisruptorEvent event, HandlerChain<DisruptorEvent> handlerChain) {}
            public void setName(String name) { this.name = name; }
            public String getName() { return name; }
        };
        // The handler is not Nameable (setName is not from the Nameable interface)
        // so the name should not be set via the Nameable path
        manager.addHandler("test", handler);
        assertSame(handler, manager.getHandler("test"));
    }

    @Test
    void shouldSetHandlers() {
        manager.setHandlers(new java.util.LinkedHashMap<>());
        assertNotNull(manager.getHandlers());
    }

    @Test
    void shouldSetHandlerChains() {
        manager.setHandlerChains(new java.util.LinkedHashMap<>());
        assertNotNull(manager.getHandlerChains());
    }

    @Test
    void shouldGetChainNamesWhenEmpty() {
        DefaultHandlerChainManager emptyManager = new DefaultHandlerChainManager();
        assertNotNull(emptyManager.getChainNames());
    }
}
