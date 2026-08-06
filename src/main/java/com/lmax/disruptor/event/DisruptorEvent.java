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
	protected final long timestamp;
	/** Event Topic */
	protected String topic;
	/** Event Tag */
	protected String tag;
	/** Event namespace */
	protected String namespace;
	/** Event messageId */
	protected String messageId;
	/** Event payload */
	protected Object payload;
	/** Event sequence */
	protected long sequence;

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

	/**
	 * 物理路由键：{@code namespace/topic[/tag]}，与 {@code @EventRule} 规则表达式（Ant 风格 / 分隔）保持一致。
	 * <h3>路由模型</h3>
	 * <pre>
	 *   namespace/topic/tag
	 *   └───┬───┘ └─┬─┘ └┬┘
	 *    环境隔离 业务分类 细分标签
	 * </pre>
	 * <ul>
	 *   <li>{@code namespace} —— 命名空间，用于多环境 / 多租户隔离</li>
	 *   <li>{@code topic} —— 消费线程隔离维度，不同 topic 走不同消费线程池</li>
	 *   <li>{@code tag} —— 同 topic 下共享消费线程，做消息过滤</li>
	 * </ul>
	 */
	public String getRouteExpression() {
		String base = (namespace == null ? "" : namespace) + "/" + (topic == null ? "" : topic);
		if (tag == null || StringUtils.isBlank(tag) || "*".equals(tag)) {
			return base;
		}
		return base + "/" + tag;
	}
	
	@Override
	public String toString() {
		return "DisruptorEvent [topic :" + getTopic() + ",namespace :" + getNamespace() + ",tag :" + getTag() + ", messageId :" +  getMessageId() + "]";
	}
	
}
