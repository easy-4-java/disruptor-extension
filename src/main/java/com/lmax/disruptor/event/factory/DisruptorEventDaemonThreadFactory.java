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
 * {@link ThreadFactory} that creates daemon threads for the Disruptor event
 * processing. Daemon threads do not prevent the JVM from shutting down.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see ThreadFactory
 * @see Thread#setDaemon(boolean)
 */
public class DisruptorEventDaemonThreadFactory implements ThreadFactory {

	/**
	 * Creates a new daemon {@link Thread} to execute the given
	 * {@link Runnable}.
	 *
	 * @param r the runnable to execute
	 * @return a new daemon thread
	 */
	@Override
    public Thread newThread(@NonNull Runnable r) {
		Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	}

}
