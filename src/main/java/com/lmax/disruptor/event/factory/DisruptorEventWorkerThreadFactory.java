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
package com.lmax.disruptor.event.factory;

import lombok.NonNull;

import java.util.concurrent.ThreadFactory;

/**
 * {@link ThreadFactory} that creates named worker threads with an
 * auto-incrementing counter suffix. Useful for distinguishing individual
 * consumer threads in thread dumps and log output.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see ThreadFactory
 */
public class DisruptorEventWorkerThreadFactory implements ThreadFactory {

	private int counter = 0;
	private String prefix = "";

	/**
	 * Creates a new factory with the given thread name prefix.
	 *
	 * @param prefix the prefix for generated thread names (e.g. "worker")
	 */
	public DisruptorEventWorkerThreadFactory(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Creates a new named {@link Thread} using the configured prefix and an
	 * auto-incrementing counter (e.g. "worker-0", "worker-1").
	 *
	 * @param r the runnable to execute
	 * @return a new named thread
	 */
	@Override
    public Thread newThread(@NonNull Runnable r) {
		return new Thread(r, prefix + "-" + counter++);
	}

}
