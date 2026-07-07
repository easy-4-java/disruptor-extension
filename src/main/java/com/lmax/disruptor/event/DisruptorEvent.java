package com.lmax.disruptor.event;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.EventObject;

import com.lmax.disruptor.util.StringUtils;

/**
 * 事件(Event) 就是通过 Disruptor 进行交换的数据类型。
 */
@Getter
@Setter
public class DisruptorEvent extends EventObject {

	/** System time when the event happened */
	private final long timestamp;

	/** Event Topic */
	public String topic;
	/** Event Tag */
	private String tag;
	/** Event namespace */
	public String namespace;
	/** Event messageId */
	public String messageId;
	/** Event payload */
	private Object payload;
	/** Event sequence */
	public long sequence;

	/**
	 * Create a new ConsumeEvent.
	 */
	public DisruptorEvent() {
		super(Thread.currentThread());
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * Create a new ConsumeEvent.
	 * @param source the object on which the event initially occurred (never {@code null})
	 */
	public DisruptorEvent(Object source) {
		super(source);
		this.timestamp = System.currentTimeMillis();
	}

	public String getRouteExpression() {

		String base = (namespace == null ? "" : namespace) + "." + (topic == null ? "" : topic);
		if (tag == null || StringUtils.isBlank(tag) || "*".equals(tag)) {
			return base;
		}
		return base + "." + tag;

	}
	
	@Override
	public String toString() {
		return "DisruptorEvent [topic :" + getTopic() + ",namespace :" + getNamespace() + ",tag :" + getTag() + ", messageId :" +  getMessageId() + "]";
	}
	
}
