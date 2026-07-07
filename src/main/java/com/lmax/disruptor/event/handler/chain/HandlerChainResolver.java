package com.lmax.disruptor.event.handler.chain;

import com.lmax.disruptor.event.DisruptorEvent;

public interface HandlerChainResolver<T extends DisruptorEvent> {

	HandlerChain<T> getChain(T event , HandlerChain<T> originalChain);
	
}
