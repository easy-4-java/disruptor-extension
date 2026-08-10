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

import com.lmax.disruptor.EventTranslatorThreeArg;
import com.lmax.disruptor.event.DisruptorEvent;

/**
 * {@link EventTranslatorThreeArg} implementation that populates a
 * pre-allocated ring-buffer {@link DisruptorEvent} from a topic, tag,
 * and message-key triple.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see EventTranslatorThreeArg
 * @see DisruptorEvent
 */
public class DisruptorEventThreeArgTranslator implements EventTranslatorThreeArg<DisruptorEvent, String, String, String> {

	/**
	 * Translates a topic, tag, and key into the pre-allocated ring-buffer
	 * event.
	 *
	 * @param dtEevent the pre-allocated event in the ring buffer
	 * @param sequence the sequence number of the event being published
	 * @param event    the topic to set
	 * @param tag      the tag to set
	 * @param key      the message identifier to set
	 */
	@Override
	public void translateTo(DisruptorEvent dtEevent, long sequence, String event, String tag, String key) {
		dtEevent.setTopic(event);
		dtEevent.setTag(tag);
		dtEevent.setMessageId(key);
	}

}
