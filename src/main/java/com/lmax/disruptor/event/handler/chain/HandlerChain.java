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
package com.lmax.disruptor.event.handler.chain;

import com.lmax.disruptor.event.DisruptorEvent;

/**
 * A chain of {@link com.lmax.disruptor.event.handler.DisruptorHandler}
 * instances that process a Disruptor event sequentially. Each handler in
 * the chain may delegate to the next by calling {@code doHandler} on the
 * chain again.
 *
 * @param <T> the DisruptorEvent subtype
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see com.lmax.disruptor.event.handler.DisruptorHandler
 * @see ProxiedHandlerChain
 */
public interface HandlerChain<T extends DisruptorEvent>{

	/**
	 * Processes the given event through this handler chain.
	 *
	 * @param event the event to process (must not be {@code null})
	 * @throws Exception if an error occurs during processing
	 */
	void doHandler(T event) throws Exception;

}
