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
package com.lmax.disruptor.hooks;

import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.event.DisruptorEvent;

/**
 * JVM shutdown hook that gracefully shuts down a {@link Disruptor} instance
 * when the virtual machine is terminating.
 *
 * <p>Register this hook via {@link Runtime#addShutdownHook(Thread)}.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see Disruptor#shutdown()
 * @see Runtime#addShutdownHook(Thread)
 */
public class DisruptorShutdownHook extends Thread {

	private final Disruptor<DisruptorEvent> disruptor;

	/**
	 * Creates a new shutdown hook for the given {@link Disruptor} instance.
	 *
	 * @param disruptor the Disruptor to shut down on JVM exit (must not be
	 *                  {@code null})
	 */
	public DisruptorShutdownHook(Disruptor<DisruptorEvent> disruptor) {
		this.disruptor = disruptor;
	}

	/**
	 * Shuts down the Disruptor when invoked by the JVM shutdown sequence.
	 */
	@Override
	public void run() {
		disruptor.shutdown();
	}

}
