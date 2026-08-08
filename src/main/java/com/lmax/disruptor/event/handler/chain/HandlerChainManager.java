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

import java.util.Map;
import java.util.Set;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.DisruptorHandler;
import com.lmax.disruptor.event.handler.NamedHandlerList;

/**
 * Manager responsible for creating and maintaining named handler chains.
 * Handlers are registered by name and then assembled into chains using
 * comma-separated chain definitions.
 *
 * @param <T> the DisruptorEvent subtype
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see HandlerChain
 * @see NamedHandlerList
 * @see com.lmax.disruptor.event.handler.chain.def.DefaultHandlerChainManager
 */
public interface HandlerChainManager<T extends DisruptorEvent> {

	/**
	 * Returns all registered handlers keyed by name.
	 *
	 * @return an unmodifiable map of handler name to handler instance
	 */
    Map<String, DisruptorHandler<T>> getHandlers();

    /**
     * Returns the named handler list for the given chain name, or
     * {@code null} if no chain with that name exists.
     *
     * @param chainName the chain name
     * @return the handler list, or {@code null}
     */
    NamedHandlerList<T> getChain(String chainName);

    /**
     * Returns whether any handler chains have been configured.
     *
     * @return {@code true} if at least one chain exists
     */
    boolean hasChains();

    /**
     * Returns the set of all configured chain names.
     *
     * @return the chain names
     */
    Set<String> getChainNames();

    /**
     * Wraps the given original chain with the handlers in the named chain,
     * producing a proxied chain that first executes the named handlers and
     * then delegates to the original.
     *
     * @param original  the original handler chain
     * @param chainName the name of the handler chain to proxy
     * @return a new proxied handler chain
     * @throws IllegalArgumentException if no chain with the given name
     *                                  exists
     */
    HandlerChain<T> proxy(HandlerChain<T> original, String chainName);

    /**
     * Registers a handler under the given name. If a handler with the same
     * name already exists, it is replaced.
     *
     * @param name    the handler name
     * @param handler the handler instance
     */
    void addHandler(String name, DisruptorHandler<T> handler);

    /**
     * Creates a new handler chain from a comma-separated definition
     * string. Each token in the definition is the name of a previously
     * registered handler.
     *
     * @param chainName       the name of the chain to create
     * @param chainDefinition comma-separated handler names
     * @throws NullPointerException if chainName or chainDefinition is
     *                              blank
     */
    void createChain(String chainName, String chainDefinition);

    /**
     * Appends a handler to the named chain. The handler must have been
     * previously registered via {@link #addHandler}.
     *
     * @param chainName   the chain to add to
     * @param handlerName the name of the handler to append
     * @throws IllegalArgumentException if chainName is blank or the
     *                                  handler is not registered
     */
    void addToChain(String chainName, String handlerName);

}
