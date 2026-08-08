package com.lmax.disruptor.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventHandlerDefinitionTests {

    @Test
    void shouldCreateWithDefaults() {
        EventHandlerDefinition def = new EventHandlerDefinition();
        assertEquals(0, def.getOrder());
        assertNull(def.getDefinitions());
        assertNotNull(def.getDefinitionMap());
        assertTrue(def.getDefinitionMap().isEmpty());
    }

    @Test
    void shouldSetAndGetOrder() {
        EventHandlerDefinition def = new EventHandlerDefinition();
        def.setOrder(5);
        assertEquals(5, def.getOrder());
    }

    @Test
    void shouldSetAndGetDefinitions() {
        EventHandlerDefinition def = new EventHandlerDefinition();
        def.setDefinitions("handlerA,handlerB");
        assertEquals("handlerA,handlerB", def.getDefinitions());
    }

    @Test
    void shouldSetAndGetDefinitionMap() {
        EventHandlerDefinition def = new EventHandlerDefinition();
        def.getDefinitionMap().put("/topic/**", "handlerA,handlerB");
        assertEquals(1, def.getDefinitionMap().size());
        assertEquals("handlerA,handlerB", def.getDefinitionMap().get("/topic/**"));
    }
}
