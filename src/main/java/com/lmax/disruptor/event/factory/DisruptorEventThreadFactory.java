package com.lmax.disruptor.event.factory;

import lombok.NonNull;

import java.util.concurrent.ThreadFactory;

public class DisruptorEventThreadFactory implements ThreadFactory {

	@Override
	public Thread newThread(@NonNull Runnable r) {
		return new Thread(r);
	}

}
