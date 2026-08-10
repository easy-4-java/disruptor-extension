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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.chain.HandlerChain;

/**
 * Abstract handler that adds an {@code enabled} flag. When the handler is
 * disabled, events pass through to the next handler in the chain without
 * being processed by this handler.
 *
 * @param <T> the DisruptorEvent subtype
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see AbstractNameableEventHandler
 * @see AbstractAdviceEventHandler
 */
@Slf4j
public abstract class AbstractEnabledEventHandler<T extends DisruptorEvent> extends AbstractNameableEventHandler<T> {

	/**
	 * Whether this handler is enabled. Defaults to {@code true}.
	 */
	@Getter
	@Setter
	protected boolean enabled = true;

	/**
	 * Template method that performs the actual event handling logic. Called
	 * only when this handler is enabled.
	 *
	 * @param event        the event to process
	 * @param handlerChain the remaining handler chain
	 * @throws Exception if an error occurs during handling
	 */
	protected abstract void doHandlerInternal(T event, HandlerChain<T> handlerChain) throws Exception;

	/**
	 * Handles the event by checking the enabled flag and delegating to
	 * {@link #doHandlerInternal} if enabled, or passing through to the
	 * next handler if disabled.
	 *
	 * @param event        the event to process
	 * @param handlerChain the remaining handler chain
	 * @throws Exception if an error occurs during handling
	 */
	@Override
	public void doHandler(T event, HandlerChain<T> handlerChain) throws Exception {
		if (!isEnabled()) {
			log.debug("Handler '{}' is not enabled for the current event.  Proceeding without invoking this handler.",
					getName());
			// Proceed without invoking this handler...
			handlerChain.doHandler(event);
		} else {
			log.trace("Handler '{}' enabled.  Executing now.", getName());
			doHandlerInternal(event, handlerChain);
		}
	}

}
