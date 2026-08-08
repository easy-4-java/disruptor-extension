package com.lmax.disruptor.event.translator;

import com.lmax.disruptor.event.DisruptorEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisruptorEventTwoArgTranslatorTests {

    private final DisruptorEventTwoArgTranslator translator = new DisruptorEventTwoArgTranslator();

    @Test
    void shouldTranslateTopicAndTag() {
        DisruptorEvent event = new DisruptorEvent();
        translator.translateTo(event, 10L, "my-topic", "my-tag");
        assertEquals("my-topic", event.getTopic());
        assertEquals("my-tag", event.getTag());
        assertEquals("10", event.getMessageId());
    }
}
