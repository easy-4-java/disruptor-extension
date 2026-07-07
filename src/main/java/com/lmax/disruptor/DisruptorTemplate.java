/*
 * Copyright (c) 2017, hiwepy (https://github.com/hiwepy).
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

public class DisruptorTemplate {
	
	protected final Disruptor<DisruptorEvent> disruptor;
	protected final EventTranslatorOneArg<DisruptorEvent, DisruptorEvent> oneArgEventTranslator;

	public DisruptorTemplate(Disruptor<DisruptorEvent> disruptor, EventTranslatorOneArg<DisruptorEvent, DisruptorEvent> oneArgEventTranslator) {
		this.disruptor = disruptor;
		this.oneArgEventTranslator = oneArgEventTranslator;
	}

	public void publishEvent(DisruptorEvent event) {
		disruptor.publishEvent(oneArgEventTranslator, event);
	}
	
	public void publishEvent(String topic, String tag, Object payload) {
		DisruptorEvent bindEvent = new DisruptorEvent();
		bindEvent.setTopic(topic);
		bindEvent.setTag(tag);
		bindEvent.setPayload(payload);
		disruptor.publishEvent(oneArgEventTranslator, bindEvent);
	}
	
	public void publishEvent(String topic, String namespace, String tag, Object payload) {
		DisruptorEvent bindEvent = new DisruptorEvent();
		bindEvent.setTopic(topic);
		bindEvent.setNamespace(namespace);
		bindEvent.setTag(tag);
		bindEvent.setPayload(payload);
		bindEvent.setMessageId(String.valueOf(System.currentTimeMillis()));
		disruptor.publishEvent(oneArgEventTranslator, bindEvent);
	}

}
