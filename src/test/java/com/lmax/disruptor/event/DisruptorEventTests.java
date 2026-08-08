package com.lmax.disruptor.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisruptorEventTests {

    @Test
    void shouldCreateEventWithDefaultConstructor() {
        DisruptorEvent event = new DisruptorEvent();
        assertNotNull(event);
        assertTrue(event.getTimestamp() > 0);
        assertNotNull(event.getSource());
    }

    @Test
    void shouldCreateEventWithSource() {
        Object source = new Object();
        DisruptorEvent event = new DisruptorEvent(source);
        assertSame(source, event.getSource());
        assertTrue(event.getTimestamp() > 0);
    }

    @Test
    void shouldSetAndGetTopic() {
        DisruptorEvent event = new DisruptorEvent();
        event.setTopic("test-topic");
        assertEquals("test-topic", event.getTopic());
    }

    @Test
    void shouldSetAndGetTag() {
        DisruptorEvent event = new DisruptorEvent();
        event.setTag("test-tag");
        assertEquals("test-tag", event.getTag());
    }

    @Test
    void shouldSetAndGetNamespace() {
        DisruptorEvent event = new DisruptorEvent();
        event.setNamespace("test-ns");
        assertEquals("test-ns", event.getNamespace());
    }

    @Test
    void shouldSetAndGetMessageId() {
        DisruptorEvent event = new DisruptorEvent();
        event.setMessageId("msg-001");
        assertEquals("msg-001", event.getMessageId());
    }

    @Test
    void shouldSetAndGetPayload() {
        DisruptorEvent event = new DisruptorEvent();
        Object payload = "data";
        event.setPayload(payload);
        assertSame(payload, event.getPayload());
    }

    @Test
    void shouldSetAndGetSequence() {
        DisruptorEvent event = new DisruptorEvent();
        event.setSequence(42L);
        assertEquals(42L, event.getSequence());
    }

    @Test
    void shouldGetRouteExpressionWithAllFields() {
        DisruptorEvent event = new DisruptorEvent();
        event.setNamespace("ns");
        event.setTopic("topic");
        event.setTag("tag");
        assertEquals("ns/topic/tag", event.getRouteExpression());
    }

    @Test
    void shouldGetRouteExpressionWithoutTag() {
        DisruptorEvent event = new DisruptorEvent();
        event.setNamespace("ns");
        event.setTopic("topic");
        assertEquals("ns/topic", event.getRouteExpression());
    }

    @Test
    void shouldGetRouteExpressionWithWildcardTag() {
        DisruptorEvent event = new DisruptorEvent();
        event.setNamespace("ns");
        event.setTopic("topic");
        event.setTag("*");
        assertEquals("ns/topic", event.getRouteExpression());
    }

    @Test
    void shouldGetRouteExpressionWithBlankTag() {
        DisruptorEvent event = new DisruptorEvent();
        event.setNamespace("ns");
        event.setTopic("topic");
        event.setTag("  ");
        assertEquals("ns/topic", event.getRouteExpression());
    }

    @Test
    void shouldGetRouteExpressionWithNullNamespaceAndTopic() {
        DisruptorEvent event = new DisruptorEvent();
        assertEquals("/", event.getRouteExpression());
    }

    @Test
    void shouldGetRouteExpressionWithNullTag() {
        DisruptorEvent event = new DisruptorEvent();
        event.setNamespace("ns");
        event.setTopic("topic");
        event.setTag(null);
        assertEquals("ns/topic", event.getRouteExpression());
    }

    @Test
    void shouldReturnToString() {
        DisruptorEvent event = new DisruptorEvent();
        event.setTopic("t");
        event.setNamespace("n");
        event.setTag("tag");
        event.setMessageId("id");
        String str = event.toString();
        assertNotNull(str);
        assertTrue(str.contains("t"));
        assertTrue(str.contains("n"));
    }
}
