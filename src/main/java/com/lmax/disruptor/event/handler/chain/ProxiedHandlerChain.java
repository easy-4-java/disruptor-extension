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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.DisruptorHandler;

/**
 * {@link HandlerChain} implementation that walks through a list of
 * {@link DisruptorHandler} instances in order, delegating to the next
 * handler (or to an original chain) as each handler calls
 * {@link #doHandler(DisruptorEvent)}.
 *
 * <p>This is the core chain-of-responsibility mechanism: each handler
 * receives this chain instance and may invoke {@code doHandler} on it
 * to continue processing.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see HandlerChain
 * @see DisruptorHandler
 */
public class ProxiedHandlerChain implements HandlerChain<DisruptorEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(ProxiedHandlerChain.class);

    private ProxiedHandlerChain originalChain;
    private List<DisruptorHandler<DisruptorEvent>> handlers;
    private int currentPosition = 0;

    /**
     * Creates an empty chain used as the entry point for the
     * handler-chain framework. The position is set to -1 to indicate
     * that no handlers have been assigned yet.
     */
    public ProxiedHandlerChain() {
        this.currentPosition = -1;
    }

    /**
     * Creates a proxied chain that wraps the given original chain and
     * executes the given handlers before delegating to it.
     *
     * @param orig     the original chain to delegate to after all
     *                 handlers have executed (must not be {@code null})
     * @param handlers the list of handlers to execute in order
     * @throws NullPointerException if {@code orig} is {@code null}
     */
    public ProxiedHandlerChain(ProxiedHandlerChain orig, List<DisruptorHandler<DisruptorEvent>> handlers) {
        if (orig == null) {
            throw new NullPointerException("original HandlerChain cannot be null.");
        }
        this.originalChain = orig;
        this.handlers = handlers;
        this.currentPosition = 0;
    }

    /**
     * Processes the event by invoking the next handler in the list, or
     * delegating to the original chain when all handlers have been
     * executed.
     *
     * @param event the event to process
     * @throws Exception if an error occurs during processing
     */
    @Override
	public void doHandler(DisruptorEvent event) throws Exception {
        if (this.handlers == null || this.handlers.size() == this.currentPosition) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Invoking original filter chain.");
            }
            if(this.originalChain != null) {
            	this.originalChain.doHandler(event);
            }
        } else {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Invoking wrapped filter at index [" + this.currentPosition + "]");
            }
            this.handlers.get(this.currentPosition++).doHandler(event, this);
        }
    }

}
