package com.lmax.disruptor.event.handler.chain.def;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.DisruptorHandler;
import com.lmax.disruptor.event.handler.chain.HandlerChain;
import com.lmax.disruptor.event.handler.chain.ProxiedHandlerChain;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DefaultNamedHandlerListTests {

    @Test
    void shouldCreateWithName() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        assertEquals("test", list.getName());
        assertTrue(list.isEmpty());
    }

    @Test
    void shouldCreateWithNameAndList() {
        ArrayList<DisruptorHandler<DisruptorEvent>> handlers = new ArrayList<>();
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test", handlers);
        assertEquals("test", list.getName());
    }

    @Test
    void shouldThrowOnNullBackingList() {
        assertThrows(NullPointerException.class, () ->
                new DefaultNamedHandlerList("test", null));
    }

    @Test
    void shouldThrowOnBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                new DefaultNamedHandlerList(""));
    }

    @Test
    void shouldSetName() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("old");
        list.setName("new");
        assertEquals("new", list.getName());
    }

    @Test
    void shouldAddAndRetrieveHandlers() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        DisruptorHandler<DisruptorEvent> handler = (event, chain) -> {};
        list.add(handler);
        assertEquals(1, list.size());
        assertSame(handler, list.get(0));
    }

    @Test
    void shouldProxy() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        ProxiedHandlerChain original = new ProxiedHandlerChain();
        HandlerChain<DisruptorEvent> proxied = list.proxy(original);
        assertNotNull(proxied);
    }

    @Test
    void shouldSupportListOperations() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        DisruptorHandler<DisruptorEvent> h1 = (event, chain) -> {};
        DisruptorHandler<DisruptorEvent> h2 = (event, chain) -> {};

        list.add(h1);
        list.add(h2);
        assertEquals(2, list.size());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(h1));
        assertTrue(list.contains(h2));

        list.remove(0);
        assertEquals(1, list.size());
    }

    @Test
    void shouldSupportSet() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        DisruptorHandler<DisruptorEvent> h1 = (event, chain) -> {};
        DisruptorHandler<DisruptorEvent> h2 = (event, chain) -> {};
        list.add(h1);
        list.set(0, h2);
        assertSame(h2, list.get(0));
    }

    @Test
    void shouldSupportClear() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        list.add((event, chain) -> {});
        list.clear();
        assertTrue(list.isEmpty());
    }

    @Test
    void shouldSupportContainsAll() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        DisruptorHandler<DisruptorEvent> h1 = (event, chain) -> {};
        list.add(h1);
        assertTrue(list.containsAll(java.util.Collections.singletonList(h1)));
    }

    @Test
    void shouldSupportToArray() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        list.add((event, chain) -> {});
        assertEquals(1, list.toArray().length);
    }

    @Test
    void shouldSupportToArrayWithArray() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        list.add((event, chain) -> {});
        DisruptorHandler<DisruptorEvent>[] arr = list.toArray(new DisruptorHandler[0]);
        assertEquals(1, arr.length);
    }

    @Test
    void shouldSupportIndexOf() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        DisruptorHandler<DisruptorEvent> h1 = (event, chain) -> {};
        list.add(h1);
        assertEquals(0, list.indexOf(h1));
    }

    @Test
    void shouldSupportLastIndexOf() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        DisruptorHandler<DisruptorEvent> h1 = (event, chain) -> {};
        list.add(h1);
        assertEquals(0, list.lastIndexOf(h1));
    }

    @Test
    void shouldSupportIterator() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        list.add((event, chain) -> {});
        assertNotNull(list.iterator());
        assertTrue(list.iterator().hasNext());
    }

    @Test
    void shouldSupportListIterator() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        list.add((event, chain) -> {});
        assertNotNull(list.listIterator());
        assertNotNull(list.listIterator(0));
    }

    @Test
    void shouldSupportSubList() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        list.add((event, chain) -> {});
        list.add((event, chain) -> {});
        assertEquals(1, list.subList(0, 1).size());
    }

    @Test
    void shouldSupportAddAll() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        java.util.List<DisruptorHandler<DisruptorEvent>> toAdd = new ArrayList<>();
        toAdd.add((event, chain) -> {});
        list.addAll(toAdd);
        assertEquals(1, list.size());
    }

    @Test
    void shouldSupportAddAllAtIndex() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        list.add((event, chain) -> {});
        java.util.List<DisruptorHandler<DisruptorEvent>> toAdd = new ArrayList<>();
        toAdd.add((event, chain) -> {});
        list.addAll(0, toAdd);
        assertEquals(2, list.size());
    }

    @Test
    void shouldSupportRemoveAll() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        DisruptorHandler<DisruptorEvent> h1 = (event, chain) -> {};
        list.add(h1);
        list.removeAll(java.util.Collections.singletonList(h1));
        assertTrue(list.isEmpty());
    }

    @Test
    void shouldSupportRetainAll() {
        DefaultNamedHandlerList list = new DefaultNamedHandlerList("test");
        DisruptorHandler<DisruptorEvent> h1 = (event, chain) -> {};
        DisruptorHandler<DisruptorEvent> h2 = (event, chain) -> {};
        list.add(h1);
        list.add(h2);
        list.retainAll(java.util.Collections.singletonList(h1));
        assertEquals(1, list.size());
    }
}
