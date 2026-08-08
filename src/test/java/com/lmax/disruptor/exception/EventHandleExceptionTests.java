package com.lmax.disruptor.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventHandleExceptionTests {

    @Test
    void shouldCreateFromException() {
        Exception cause = new RuntimeException("original");
        EventHandleException ex = new EventHandleException(cause);
        assertEquals("original", ex.getMessage());
    }

    @Test
    void shouldCreateFromMessage() {
        EventHandleException ex = new EventHandleException("error occurred");
        assertEquals("error occurred", ex.getMessage());
    }

    @Test
    void shouldCreateFromMessageAndCause() {
        Throwable cause = new RuntimeException("root");
        EventHandleException ex = new EventHandleException("wrapper", cause);
        assertEquals("wrapper", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void shouldBeRuntimeException() {
        EventHandleException ex = new EventHandleException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
