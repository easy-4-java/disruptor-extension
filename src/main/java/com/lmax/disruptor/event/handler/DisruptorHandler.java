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
package com.lmax.disruptor.event.handler;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.chain.HandlerChain;

/**
 * Core handler interface for processing Disruptor events within a handler
 * chain. Each handler may delegate to the next handler in the chain by
 * calling {@link HandlerChain#doHandler(DisruptorEvent)}.
 *
 * @param <T> the DisruptorEvent subtype handled by this handler
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see HandlerChain
 * @see AbstractNameableEventHandler
 */
public interface DisruptorHandler<T extends DisruptorEvent> {

	/**
	 * Handles the given event, optionally delegating to the remainder of
	 * the handler chain.
	 *
	 * @param event       the event to process (never {@code null})
	 * @param handlerChain the remaining handler chain
	 * @throws Exception if an error occurs during handling
	 */
	public void doHandler(T event, HandlerChain<T> handlerChain) throws Exception;

}
