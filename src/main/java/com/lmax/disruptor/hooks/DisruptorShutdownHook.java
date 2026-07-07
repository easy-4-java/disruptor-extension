package com.lmax.disruptor.hooks;

import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.event.DisruptorEvent;

public class DisruptorShutdownHook extends Thread{
	
	private final Disruptor<DisruptorEvent> disruptor;
	
	public DisruptorShutdownHook(Disruptor<DisruptorEvent> disruptor) {
		this.disruptor = disruptor;
	}
	
	@Override
	public void run() {
		disruptor.shutdown();
	}
	
}
