package com.lmax.disruptor.event.handler.chain;

import com.lmax.disruptor.event.DisruptorEvent;

public interface HandlerChain<T extends DisruptorEvent>{

	void doHandler(T event) throws Exception;
	
}
