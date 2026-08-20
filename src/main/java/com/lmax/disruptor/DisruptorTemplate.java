/*
 * Copyright (c) 2017, Loong Wan (https://github.com/loong10k).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.lmax.disruptor;


import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.event.DisruptorEvent;

import java.util.UUID;

/**
 * Convenience template that wraps a {@link Disruptor} and an
 * {@link EventTranslatorOneArg} to simplify publishing
 * {@link DisruptorEvent}s to the ring buffer.
 *
 * <p>Provides overloaded {@code publishEvent} methods that accept
 * topic / namespace / tag / payload parameters and automatically
 * populate a {@link DisruptorEvent} before delegating to the
 * underlying Disruptor.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see Disruptor
 * @see DisruptorEvent
 * @see EventTranslatorOneArg
 */
public class DisruptorTemplate {

	/** The underlying Disruptor instance. */
	protected final Disruptor<DisruptorEvent> disruptor;

	/** The translator used to copy bind-event fields into the ring-buffer slot. */
	protected final EventTranslatorOneArg<DisruptorEvent, DisruptorEvent> oneArgEventTranslator;

	/**
	 * Creates a new template backed by the given Disruptor and translator.
	 *
	 * @param disruptor            the Disruptor instance (must not be
	 *                             {@code null})
	 * @param oneArgEventTranslator the translator for one-arg publishing
	 *                              (must not be {@code null})
	 */
	public DisruptorTemplate(Disruptor<DisruptorEvent> disruptor, EventTranslatorOneArg<DisruptorEvent, DisruptorEvent> oneArgEventTranslator) {
		this.disruptor = disruptor;
		this.oneArgEventTranslator = oneArgEventTranslator;
	}

	/**
	 * Publishes the given {@link DisruptorEvent} to the ring buffer.
	 *
	 * @param event the event to publish (must not be {@code null})
	 */
	public void publishEvent(DisruptorEvent event) {
		disruptor.publishEvent(oneArgEventTranslator, event);
	}

	/**
	 * Creates a new {@link DisruptorEvent} with the given topic, tag, and
	 * payload, then publishes it to the ring buffer. A unique message
	 * identifier based on {@link UUID} is assigned automatically so that
	 * consumers can correlate events even under high-concurrency bursts
	 * where multiple events are published within the same wall-clock
	 * millisecond.
	 *
	 * @param topic   the event topic
	 * @param tag     the event tag
	 * @param payload the event payload
	 */
	public void publishEvent(String topic, String tag, Object payload) {
		DisruptorEvent bindEvent = new DisruptorEvent();
		bindEvent.setTopic(topic);
		bindEvent.setTag(tag);
		bindEvent.setPayload(payload);
		bindEvent.setMessageId(UUID.randomUUID().toString());
		disruptor.publishEvent(oneArgEventTranslator, bindEvent);
	}

	/**
	 * Creates a new {@link DisruptorEvent} with the given topic, namespace,
	 * tag, and payload, then publishes it to the ring buffer. A unique
	 * message identifier based on {@link UUID} is assigned automatically,
	 * avoiding the high-concurrency collisions that would occur if a
	 * millisecond-resolution wall-clock timestamp were used as the ID.
	 *
	 * @param topic     the event topic
	 * @param namespace the event namespace
	 * @param tag       the event tag
	 * @param payload   the event payload
	 */
	public void publishEvent(String topic, String namespace, String tag, Object payload) {
		DisruptorEvent bindEvent = new DisruptorEvent();
		bindEvent.setTopic(topic);
		bindEvent.setNamespace(namespace);
		bindEvent.setTag(tag);
		bindEvent.setPayload(payload);
		bindEvent.setMessageId(UUID.randomUUID().toString());
		disruptor.publishEvent(oneArgEventTranslator, bindEvent);
	}

}
