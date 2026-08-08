package com.lmax.disruptor.annotation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventRuleTests {

    @Test
    void shouldHaveDefaultWildcardValue() throws NoSuchMethodException {
        EventRule rule = AnnotatedClass.class.getAnnotation(EventRule.class);
        assertNotNull(rule);
        assertEquals("*", rule.value());
    }

    @Test
    void shouldReadCustomValue() {
        EventRule rule = CustomAnnotatedClass.class.getAnnotation(EventRule.class);
        assertNotNull(rule);
        assertEquals("/topic/tag/**", rule.value());
    }

    @Test
    void shouldBeInherited() {
        EventRule rule = ChildClass.class.getAnnotation(EventRule.class);
        assertNotNull(rule);
        assertEquals("/inherited/**", rule.value());
    }

    @EventRule
    static class AnnotatedClass {}

    @EventRule("/topic/tag/**")
    static class CustomAnnotatedClass {}

    @EventRule("/inherited/**")
    static class ParentClass {}

    static class ChildClass extends ParentClass {}
}
