package com.lmax.disruptor.event.translator;

import com.lmax.disruptor.event.DisruptorEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisruptorEventThreeArgTranslatorTests {

    private final DisruptorEventThreeArgTranslator translator = new DisruptorEventThreeArgTranslator();

    @Test
    void shouldTranslateTopicTagAndKey() {
        DisruptorEvent event = new DisruptorEvent();
        translator.translateTo(event, 7L, "topic", "tag", "key-1");
        assertEquals("topic", event.getTopic());
        assertEquals("tag", event.getTag());
        assertEquals("key-1", event.getMessageId());
    }
}
