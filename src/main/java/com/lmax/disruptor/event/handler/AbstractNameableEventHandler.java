package com.lmax.disruptor.event.handler;

import com.lmax.disruptor.event.DisruptorEvent;

public abstract class AbstractNameableEventHandler<T extends DisruptorEvent> implements DisruptorHandler<T>, Nameable {

	/**
	 * 过滤器名称
	 */
	protected String name;

	protected String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}
