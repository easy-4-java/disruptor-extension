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
package com.lmax.disruptor.event.handler.chain.def;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.DisruptorHandler;
import com.lmax.disruptor.event.handler.Nameable;
import com.lmax.disruptor.event.handler.NamedHandlerList;
import com.lmax.disruptor.event.handler.chain.HandlerChain;
import com.lmax.disruptor.event.handler.chain.HandlerChainManager;
import org.apache.commons.lang3.StringUtils;

/**
 * Default {@link HandlerChainManager} implementation backed by
 * {@link LinkedHashMap}s for handler registration and chain assembly.
 *
 * <p>Handlers are registered by name and then assembled into named chains
 * via comma-separated definitions (e.g. {@code "handlerA,handlerB"}).</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see HandlerChainManager
 * @see DefaultNamedHandlerList
 * @see PathMatchingHandlerChainResolver
 */
@Getter
@Slf4j
public class DefaultHandlerChainManager implements HandlerChainManager<DisruptorEvent> {

    private Map<String, DisruptorHandler<DisruptorEvent>> handlers;

    private Map<String, NamedHandlerList<DisruptorEvent>> handlerChains;

    private final static String DEFAULT_CHAIN_DEFINATION_DELIMITER_CHAR = ",";

    /**
     * Creates a new empty manager.
     */
    public DefaultHandlerChainManager() {
        this.handlers = new LinkedHashMap<String, DisruptorHandler<DisruptorEvent>>();
        this.handlerChains = new LinkedHashMap<String, NamedHandlerList<DisruptorEvent>>();
    }

    /**
     * Replaces the handler registry.
     *
     * @param handlers the new handler map
     */
    public void setHandlers(Map<String, DisruptorHandler<DisruptorEvent>> handlers) {
        this.handlers = handlers;
    }

    /**
     * Replaces the handler chain map.
     *
     * @param handlerChains the new handler chain map
     */
    public void setHandlerChains(Map<String, NamedHandlerList<DisruptorEvent>> handlerChains) {
        this.handlerChains = handlerChains;
    }

    /**
     * Returns the handler registered under the given name.
     *
     * @param name the handler name
     * @return the handler, or {@code null} if not registered
     */
    public DisruptorHandler<DisruptorEvent> getHandler(String name) {
        return this.handlers.get(name);
    }

    /**
     * Registers a handler under the given name, replacing any existing
     * handler with the same name.
     *
     * @param name    the handler name
     * @param handler the handler instance
     */
    @Override
    public void addHandler(String name, DisruptorHandler<DisruptorEvent> handler) {
        addHandler(name, handler, true);
    }

    protected void addHandler(String name, DisruptorHandler<DisruptorEvent> handler, boolean overwrite) {
        DisruptorHandler<DisruptorEvent> existing = getHandler(name);
        if (existing == null || overwrite) {
            if (handler instanceof Nameable) {
                ((Nameable) handler).setName(name);
            }
            this.handlers.put(name, handler);
        }
    }

    /**
     * Creates a handler chain from a comma-separated definition string.
     *
     * @param chainName       the chain name (must not be blank)
     * @param chainDefinition comma-separated handler names (must not be
     *                        blank)
     * @throws NullPointerException if chainName or chainDefinition is
     *                              blank
     */
    @Override
    public void createChain(String chainName, String chainDefinition) {
        if (StringUtils.isBlank(chainName)) {
            throw new NullPointerException("chainName cannot be null or empty.");
        }
        if (StringUtils.isBlank(chainDefinition)) {
            throw new NullPointerException("chainDefinition cannot be null or empty.");
        }
        if (log.isDebugEnabled()) {
            log.debug("Creating chain [{}] from String definition [{}]", chainName, chainDefinition);
        }
        String[] handlerTokens = splitChainDefinition(chainDefinition);
        for (String token : handlerTokens) {
            addToChain(chainName, token);
        }
    }

    /**
     * Splits the comma-delimited handler chain definition line into
     * individual handler definition tokens.
     *
     * @param chainDefinition chain definition line
     * @return array of chain definition tokens
     */
    protected String[] splitChainDefinition(String chainDefinition) {
    	String trimToNull = StringUtils.trimToNull(chainDefinition);
    	if(trimToNull == null){
    		return null;
    	}
    	String[] split = (StringUtils.isEmpty(trimToNull) || StringUtils.isEmpty(DEFAULT_CHAIN_DEFINATION_DELIMITER_CHAR))
    		? new String[0]
    		: trimToNull.split(DEFAULT_CHAIN_DEFINATION_DELIMITER_CHAR);
    	for (int i = 0; i < split.length; i++) {
    		split[i] = StringUtils.trimToNull(split[i]);
		}
        return split;
    }

    /**
     * Appends a handler to the named chain.
     *
     * @param chainName   the chain to add to
     * @param handlerName the name of a previously registered handler
     * @throws IllegalArgumentException if chainName is blank or the
     *                                  handler is not registered
     */
    @Override
    public void addToChain(String chainName, String handlerName) {
        if (StringUtils.isBlank(chainName)) {
            throw new IllegalArgumentException("chainName cannot be null or empty.");
        }
        DisruptorHandler<DisruptorEvent> handler = getHandler(handlerName);
        if (handler == null) {
            throw new IllegalArgumentException("There is no handler with name '" + handlerName +
                    "' to apply to chain [" + chainName + "] in the pool of available Handlers.  Ensure a " +
                    "handler with that name/path has first been registered with the addHandler method(s).");
        }
        NamedHandlerList<DisruptorEvent> chain = ensureChain(chainName);
        chain.add(handler);
    }

    protected NamedHandlerList<DisruptorEvent> ensureChain(String chainName) {
        NamedHandlerList<DisruptorEvent> chain = getChain(chainName);
        if (chain == null) {
            chain = new DefaultNamedHandlerList(chainName);
            this.handlerChains.put(chainName, chain);
        }
        return chain;
    }

    /**
     * Returns the named handler list for the given chain, or
     * {@code null} if no chain with that name exists.
     *
     * @param chainName the chain name
     * @return the handler list, or {@code null}
     */
    @Override
    public NamedHandlerList<DisruptorEvent> getChain(String chainName) {
        return this.handlerChains.get(chainName);
    }

    /**
     * Returns whether any handler chains have been configured.
     *
     * @return {@code true} if at least one chain exists
     */
    @Override
    public boolean hasChains() {
        return this.handlerChains != null && !this.handlerChains.isEmpty();
    }

    /**
     * Returns the set of all configured chain names.
     *
     * @return the chain names
     */
    @Override
    @SuppressWarnings("unchecked")
	public Set<String> getChainNames() {
        return this.handlerChains != null ? this.handlerChains.keySet() : Collections.EMPTY_SET;
    }

    /**
     * Wraps the given original chain with the handlers in the named chain.
     *
     * @param original  the original handler chain
     * @param chainName the chain to proxy
     * @return a new proxied handler chain
     * @throws IllegalArgumentException if no chain with the given name
     *                                  exists
     */
    @Override
    public HandlerChain<DisruptorEvent> proxy(HandlerChain<DisruptorEvent> original, String chainName) {
        NamedHandlerList<DisruptorEvent> configured = getChain(chainName);
        if (configured == null) {
            String msg = "There is no configured chain under the name/key [" + chainName + "].";
            throw new IllegalArgumentException(msg);
        }
        return configured.proxy(original);
    }


}
