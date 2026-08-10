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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.chain.HandlerChain;
import com.lmax.disruptor.event.handler.chain.HandlerChainResolver;
import com.lmax.disruptor.exception.EventHandleException;

/**
 * Abstract handler that resolves and executes a {@link HandlerChain} via a
 * {@link HandlerChainResolver}. This allows different handler chains to be
 * selected at runtime based on the event's routing expression.
 *
 * @param <T> the DisruptorEvent subtype
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see AbstractEnabledEventHandler
 * @see HandlerChainResolver
 * @see DisruptorEventDispatcher
 */
public class AbstractRouteableEventHandler<T extends DisruptorEvent> extends AbstractEnabledEventHandler<T> {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractRouteableEventHandler.class);

	/**
	 * Resolver used to select the appropriate handler chain for a given
	 * event.
	 */
	protected HandlerChainResolver<T> handlerChainResolver;

	/**
	 * Creates a new instance with no resolver configured.
	 */
	public AbstractRouteableEventHandler() {
		super();
	}

	/**
	 * Creates a new instance with the given handler chain resolver.
	 *
	 * @param handlerChainResolver the resolver to use (may be {@code null})
	 */
	public AbstractRouteableEventHandler(HandlerChainResolver<T> handlerChainResolver) {
		super();
		this.handlerChainResolver = handlerChainResolver;
	}

	/**
	 * Handles the event by resolving the appropriate chain and executing it.
	 *
	 * @param event        the event to process
	 * @param handlerChain the default handler chain
	 * @throws Exception if an error occurs during handling
	 */
	@Override
	protected void doHandlerInternal(T event, HandlerChain<T> handlerChain) throws Exception {
		Throwable t = null;
		try {
			this.executeChain(event, handlerChain);
		} catch (Throwable throwable) {
			t = throwable;
		}
		if (t != null) {
			if (t instanceof IOException) {
				throw (IOException) t;
			}
			String msg = "Handlered event failed.";
			throw new EventHandleException(msg, t);
		}
	}

	/**
	 * Resolves the handler chain for the given event. If no resolver is
	 * configured or the resolver returns {@code null}, the original chain
	 * is used.
	 *
	 * @param event     the event to resolve the chain for
	 * @param origChain the default handler chain
	 * @return the resolved handler chain
	 */
	protected HandlerChain<T> getExecutionChain(T event, HandlerChain<T> origChain) {
		HandlerChain<T> chain = origChain;

		HandlerChainResolver<T> resolver = getHandlerChainResolver();
		if (resolver == null) {
			LOG.debug("No HandlerChainResolver configured.  Returning original HandlerChain.");
			return origChain;
		}

		HandlerChain<T> resolved = resolver.getChain(event, origChain);
		if (resolved != null) {
			LOG.trace("Resolved a configured HandlerChain for the current event.");
			chain = resolved;
		} else {
			LOG.trace("No HandlerChain configured for the current event.  Using the default.");
		}

		return chain;
	}

	/**
	 * Resolves and executes the handler chain for the given event.
	 *
	 * @param event     the event to process
	 * @param origChain the default handler chain
	 * @throws Exception if an error occurs during execution
	 */
	protected void executeChain(T event, HandlerChain<T> origChain) throws Exception {
		HandlerChain<T> chain = getExecutionChain(event, origChain);
		chain.doHandler(event);
	}

	/**
	 * Returns the handler chain resolver.
	 *
	 * @return the resolver, or {@code null} if not configured
	 */
	public HandlerChainResolver<T> getHandlerChainResolver() {
		return handlerChainResolver;
	}

	/**
	 * Sets the handler chain resolver.
	 *
	 * @param handlerChainResolver the resolver (may be {@code null})
	 */
	public void setHandlerChainResolver(HandlerChainResolver<T> handlerChainResolver) {
		this.handlerChainResolver = handlerChainResolver;
	}

	/**
	 * Returns the ordering value for this handler. Defaults to 0.
	 *
	 * @return the order
	 */
	public int getOrder() {
		return 0;
	}

}
