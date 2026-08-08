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

import lombok.extern.slf4j.Slf4j;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.chain.HandlerChain;

/**
 * Abstract handler providing an AOP-style lifecycle with
 * {@link #preHandle}, {@link #postHandle}, and {@link #afterCompletion}
 * callbacks around the actual chain execution.
 *
 * <p>Subclasses can override any of the three callbacks to add
 * cross-cutting behaviour such as logging, metrics, or error handling.</p>
 *
 * @param <T> the DisruptorEvent subtype
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see AbstractEnabledEventHandler
 * @see AbstractPathMatchEventHandler
 */
@Slf4j
public class AbstractAdviceEventHandler<T extends DisruptorEvent> extends AbstractEnabledEventHandler<T> {

	/**
	 * Called before the handler chain executes. Returning {@code false}
	 * prevents the chain from executing.
	 *
	 * @param event the event being processed
	 * @return {@code true} to continue chain execution, {@code false} to
	 *         abort
	 * @throws Exception if an error occurs
	 */
	protected boolean preHandle(T event) throws Exception {
		return true;
	}

	/**
	 * Called after the handler chain executes successfully.
	 *
	 * @param event the event being processed
	 * @throws Exception if an error occurs
	 */
	protected void postHandle(T event) throws Exception {
	}

	/**
	 * Called after the handler chain completes, regardless of success or
	 * failure. Always invoked in a finally block.
	 *
	 * @param event     the event being processed
	 * @param exception the exception thrown during processing, or
	 *                  {@code null} if successful
	 * @throws Exception if an error occurs
	 */
	public void afterCompletion(T event, Exception exception) throws Exception {
	}

	/**
	 * Executes the given handler chain for the event. Subclasses may
	 * override to add pre/post processing around the chain invocation.
	 *
	 * @param event the event being processed
	 * @param chain the handler chain to execute
	 * @throws Exception if an error occurs
	 */
	protected void executeChain(T event, HandlerChain<T> chain) throws Exception {
		chain.doHandler(event);
	}

	/**
	 * Template method implementing the AOP-style handler lifecycle.
	 *
	 * @param event        the event to process
	 * @param handlerChain the remaining handler chain
	 * @throws Exception if an error occurs
	 */
	@Override
	public void doHandlerInternal(T event, HandlerChain<T> handlerChain) throws Exception {

		if (!isEnabled()) {
        	log.debug("Handler '{}' is not enabled for the current event.  Proceeding without invoking this handler.", getName());
        	// Proceed without invoking this handler...
            handlerChain.doHandler(event);
		} else {

			log.trace("Handler '{}' enabled.  Executing now.", getName());

			Exception exception = null;

			try {

				boolean continueChain = preHandle(event);
				if (log.isTraceEnabled()) {
					log.trace("Invoked preHandle method.  Continuing chain?: [" + continueChain + "]");
				}
				if (continueChain) {
					executeChain(event, handlerChain);
				}
				postHandle(event);
				if (log.isTraceEnabled()) {
					log.trace("Successfully invoked postHandle method");
				}

			} catch (Exception e) {
				exception = e;
			} finally {
				cleanup(event, exception);
			}
		}

	}

	/**
	 * Invokes {@link #afterCompletion} and handles any exception it may
	 * throw.
	 *
	 * @param event     the event being processed
	 * @param existing  the exception from the handler chain, or
	 *                  {@code null}
	 * @throws Exception if afterCompletion throws and no prior exception
	 *                   exists
	 */
	protected void cleanup(T event, Exception existing) throws Exception {
		Exception exception = existing;
		try {
			afterCompletion(event, exception);
			if (log.isTraceEnabled()) {
				log.trace("Successfully invoked afterCompletion method.");
			}
		} catch (Exception e) {
			if (exception == null) {
				exception = e;
			} else {
				log.debug("afterCompletion implementation threw an exception.  This will be ignored to "
						+ "allow the original source exception to be propagated.", e);
			}
		}
	}

}
