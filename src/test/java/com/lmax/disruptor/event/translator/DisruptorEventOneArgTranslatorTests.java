package com.lmax.disruptor.event.translator;

import com.lmax.disruptor.event.DisruptorEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisruptorEventOneArgTranslatorTests {

    private final DisruptorEventOneArgTranslator translator = new DisruptorEventOneArgTranslator();

    @Test
    void shouldTranslateAllFields() {
        DisruptorEvent slot = new DisruptorEvent();
        DisruptorEvent bind = new DisruptorEvent();
        bind.setTopic("topic");
        bind.setNamespace("ns");
        bind.setTag("tag");
        bind.setMessageId("msg-1");
        bind.setPayload("data");

        translator.translateTo(slot, 5L, bind);

        assertEquals("topic", slot.getTopic());
        assertEquals("ns", slot.getNamespace());
        assertEquals("tag", slot.getTag());
        assertEquals("msg-1", slot.getMessageId());
        assertEquals("data", slot.getPayload());
        assertEquals(5L, slot.getSequence());
    }

    @Test
    void shouldUseSequenceAsMessageIdWhenBlank() {
        DisruptorEvent slot = new DisruptorEvent();
        DisruptorEvent bind = new DisruptorEvent();
        bind.setTopic("topic");
        bind.setTag("tag");
        bind.setMessageId(null);

        translator.translateTo(slot, 42L, bind);

        assertEquals("42", slot.getMessageId());
    }

    @Test
    void shouldUseSequenceAsMessageIdWhenEmpty() {
        DisruptorEvent slot = new DisruptorEvent();
        DisruptorEvent bind = new DisruptorEvent();
        bind.setTopic("topic");
        bind.setTag("tag");
        bind.setMessageId("  ");

        translator.translateTo(slot, 99L, bind);

        assertEquals("99", slot.getMessageId());
    }
}
