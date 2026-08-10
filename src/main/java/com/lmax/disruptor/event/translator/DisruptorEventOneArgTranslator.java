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
package com.lmax.disruptor.event.translator;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.util.StringUtils;

/**
 * {@link EventTranslatorOneArg} implementation that copies all fields from a
 * bind {@link DisruptorEvent} into a pre-allocated ring-buffer slot event.
 *
 * <p>If the bind event's {@code messageId} is blank, the Disruptor
 * {@code sequence} number is used as the fallback message identifier.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see EventTranslatorOneArg
 * @see DisruptorEvent
 */
public class DisruptorEventOneArgTranslator implements EventTranslatorOneArg<DisruptorEvent, DisruptorEvent> {

	/**
	 * Translates the bind event's fields into the pre-allocated ring-buffer
	 * event slot.
	 *
	 * @param event    the pre-allocated event in the ring buffer
	 * @param sequence the sequence number of the event being published
	 * @param bind     the source event whose fields are copied
	 */
	@Override
	public void translateTo(DisruptorEvent event, long sequence, DisruptorEvent bind) {
		event.setTopic(bind.getTopic());
		event.setNamespace(bind.getNamespace());
		event.setTag(bind.getTag());
		event.setMessageId(StringUtils.hasText(bind.getMessageId()) ? bind.getMessageId() : String.valueOf(sequence));
		event.setPayload(bind.getPayload());
		event.setSequence(sequence);
	}

}
