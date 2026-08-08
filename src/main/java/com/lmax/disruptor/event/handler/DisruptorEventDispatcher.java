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

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.chain.HandlerChain;
import com.lmax.disruptor.event.handler.chain.HandlerChainResolver;
import com.lmax.disruptor.event.handler.chain.ProxiedHandlerChain;

/**
 * Entry-point {@link EventHandler} that dispatches Disruptor events through
 * a responsibility chain. On each event, a fresh {@link ProxiedHandlerChain}
 * is created and the resolved handler chain is executed.
 *
 * <p>This class bridges the LMAX Disruptor {@link EventHandler} SPI with
 * the handler-chain framework provided by this extension library.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see EventHandler
 * @see AbstractRouteableEventHandler
 * @see HandlerChainResolver
 * @see ProxiedHandlerChain
 */
public class DisruptorEventDispatcher extends AbstractRouteableEventHandler<DisruptorEvent> implements EventHandler<DisruptorEvent> {

	private int order = 0;

	/**
	 * Creates a new dispatcher with the given chain resolver and order.
	 *
	 * @param filterChainResolver the resolver that selects the handler chain
	 *                            for each event (must not be {@code null})
	 * @param order               the ordering value for this dispatcher
	 */
	public DisruptorEventDispatcher(HandlerChainResolver<DisruptorEvent> filterChainResolver,int order) {
		super(filterChainResolver);
		this.order = order;
	}

	/**
	 * Disruptor callback entry point. Creates a new proxied handler chain
	 * and delegates to the handler chain framework.
	 *
	 * @param event      the event to process
	 * @param sequence   the sequence of the event in the ring buffer
	 * @param endOfBatch whether this event is the last in the current batch
	 * @throws Exception if an error occurs during handling
	 */
	@Override
	public void onEvent(DisruptorEvent event, long sequence, boolean endOfBatch) throws Exception {

		//Construct the original chain
		HandlerChain<DisruptorEvent> originalChain = new ProxiedHandlerChain();
		//Execute the event handling chain
		this.doHandler(event, originalChain);

	}

	/**
	 * Returns the ordering value for this dispatcher.
	 *
	 * @return the order
	 */
	@Override
	public int getOrder() {
		return order;
	}

}
