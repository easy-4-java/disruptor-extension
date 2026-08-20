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
package com.lmax.disruptor.event;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.EventObject;

import org.apache.commons.lang3.StringUtils;

/**
 * Core event data carrier exchanged through the LMAX Disruptor ring buffer.
 *
 * <p>Every event published to the Disruptor is wrapped in a
 * {@code DisruptorEvent} which carries routing metadata (topic, namespace,
 * tag) together with the application payload.</p>
 *
 * <h3>Routing model</h3>
 * <pre>
 *   namespace/topic/tag
 *   └───┬───┘ └─┬─┘ └┬┘
 *    env-isolation  business  sub-tag
 * </pre>
 * <ul>
 *   <li>{@code namespace} -- environment or tenant isolation</li>
 *   <li>{@code topic} -- consumer-thread isolation dimension</li>
 *   <li>{@code tag} -- message filtering within a topic</li>
 * </ul>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see DisruptorEventFactory
 * @see DisruptorEventPublisher
 */
@Getter
@Setter
public class DisruptorEvent extends EventObject {

	/** System time when the event happened. */
	protected final long timestamp;
	/** Event topic -- consumer-thread isolation dimension. */
	protected String topic;
	/** Event tag -- sub-classification within a topic. */
	protected String tag;
	/** Event namespace -- environment or tenant isolation. */
	protected String namespace;
	/** Event message identifier. */
	protected String messageId;
	/** Event payload carrying the application data. */
	protected Object payload;
	/** Sequence number assigned by the Disruptor ring buffer. */
	protected long sequence;

	/**
	 * Creates a new {@code DisruptorEvent} with the current thread as the
	 * source.
	 */
	public DisruptorEvent() {
		super(Thread.currentThread());
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * Creates a new {@code DisruptorEvent} with the specified source object.
	 *
	 * @param source the object on which the event initially occurred
	 *               (never {@code null})
	 */
	public DisruptorEvent(Object source) {
		super(source);
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * Returns the physical routing key in the form
	 * {@code namespace/topic[/tag]}, compatible with {@code @EventRule}
	 * Ant-style path expressions.
	 *
	 * @return the routing expression string
	 */
	public String getRouteExpression() {
		String base = (namespace == null ? "" : namespace) + "/" + (topic == null ? "" : topic);
		if (tag == null || StringUtils.isBlank(tag) || "*".equals(tag)) {
			return base;
		}
		return base + "/" + tag;
	}

	/**
	 * Returns a string representation of this event for debugging purposes.
	 *
	 * @return a debug-friendly string containing topic, namespace, tag, and
	 *         messageId
	 */
	@Override
	public String toString() {
		return "DisruptorEvent [topic :" + getTopic() + ",namespace :" + getNamespace() + ",tag :" + getTag() + ", messageId :" +  getMessageId() + "]";
	}

}
