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
 * Default {@link ThreadFactory} for the Disruptor event processing. Creates
 * standard (non-daemon, default-priority) threads.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see ThreadFactory
 */
public class DisruptorEventThreadFactory implements ThreadFactory {

	/**
	 * Creates a new {@link Thread} with default settings.
	 *
	 * @param r the runnable to execute
	 * @return a new thread
	 */
	@Override
	public Thread newThread(@NonNull Runnable r) {
		return new Thread(r);
	}

}
