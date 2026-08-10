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

import com.lmax.disruptor.util.AntPathMatcher;
import com.lmax.disruptor.util.PathMatcher;
import lombok.extern.slf4j.Slf4j;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.chain.HandlerChain;
import com.lmax.disruptor.event.handler.chain.HandlerChainManager;
import com.lmax.disruptor.event.handler.chain.HandlerChainResolver;

/**
 * {@link HandlerChainResolver} implementation that matches the event's
 * routing expression against Ant-style chain-name patterns managed by
 * a {@link HandlerChainManager}.
 *
 * <p>When a chain name pattern matches the event's routing expression,
 * the corresponding handler chain is proxied and returned.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see HandlerChainResolver
 * @see HandlerChainManager
 * @see AntPathMatcher
 */
@Slf4j
public class PathMatchingHandlerChainResolver implements HandlerChainResolver<DisruptorEvent> {

	/**
	 * The handler chain manager that holds the registered chains.
	 */
	private HandlerChainManager<DisruptorEvent> handlerChainManager;

	/**
	 * The path matcher used for pattern matching.
	 */
	private PathMatcher pathMatcher;

	/**
	 * Creates a new resolver with default
	 * {@link DefaultHandlerChainManager} and {@link AntPathMatcher}.
	 */
	 public PathMatchingHandlerChainResolver() {
        this.pathMatcher = new AntPathMatcher();
        this.handlerChainManager = new DefaultHandlerChainManager();
    }

	/**
	 * Returns the handler chain manager.
	 *
	 * @return the handler chain manager
	 */
	public HandlerChainManager<DisruptorEvent> getHandlerChainManager() {
		return handlerChainManager;
	}

	/**
	 * Sets the handler chain manager.
	 *
	 * @param handlerChainManager the manager (must not be {@code null})
	 */
	public void setHandlerChainManager(HandlerChainManager<DisruptorEvent> handlerChainManager) {
		this.handlerChainManager = handlerChainManager;
	}

	/**
	 * Returns the path matcher.
	 *
	 * @return the path matcher
	 */
	public PathMatcher getPathMatcher() {
		return pathMatcher;
	}

	/**
	 * Sets the path matcher.
	 *
	 * @param pathMatcher the path matcher (must not be {@code null})
	 */
	public void setPathMatcher(PathMatcher pathMatcher) {
		this.pathMatcher = pathMatcher;
	}


	/**
	 * Resolves the handler chain for the given event by matching the
	 * event's routing expression against the configured chain name
	 * patterns.
	 *
	 * @param event         the event to resolve the chain for
	 * @param originalChain the default handler chain
	 * @return the resolved handler chain, or {@code null} if no pattern
	 *         matches
	 */
	@Override
    public HandlerChain<DisruptorEvent> getChain(DisruptorEvent event, HandlerChain<DisruptorEvent> originalChain) {
        HandlerChainManager<DisruptorEvent> handlerChainManager = getHandlerChainManager();
        if (!handlerChainManager.hasChains()) {
            return null;
        }
        String eventURI = getPathWithinEvent(event);
        for (String pathPattern : handlerChainManager.getChainNames()) {
            if (pathMatches(pathPattern, eventURI)) {
                if (log.isTraceEnabled()) {
                    log.trace("Matched path pattern [" + pathPattern + "] for eventURI [" + eventURI + "].  " +
                            "Utilizing corresponding handler chain...");
                }
                return handlerChainManager.proxy(originalChain, pathPattern);
            }
        }
        return null;
    }

    /**
     * Tests whether the given pattern matches the given path.
     *
     * @param pattern the Ant-style pattern
     * @param path    the path to test
     * @return {@code true} if the pattern matches
     */
    protected boolean pathMatches(String pattern, String path) {
        PathMatcher pathMatcher = getPathMatcher();
        return pathMatcher.match(pattern, path);
    }

    /**
     * Extracts the routing expression from the event.
     *
     * @param event the event
     * @return the routing expression
     */
    protected String getPathWithinEvent(DisruptorEvent event) {
    	return event.getRouteExpression();
    }

}
