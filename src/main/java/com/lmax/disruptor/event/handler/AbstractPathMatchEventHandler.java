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

import java.util.ArrayList;
import java.util.List;

import com.lmax.disruptor.util.AntPathMatcher;
import com.lmax.disruptor.util.PathMatcher;
import lombok.extern.slf4j.Slf4j;

import com.lmax.disruptor.event.DisruptorEvent;

/**
 * Abstract handler that applies only to events whose routing expression
 * matches one or more configured Ant-style path patterns. If no patterns
 * are configured, the handler passes through immediately.
 *
 * @param <T> the DisruptorEvent subtype
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see AbstractAdviceEventHandler
 * @see PathProcessor
 * @see AntPathMatcher
 */
@Slf4j
public abstract class AbstractPathMatchEventHandler<T extends DisruptorEvent> extends AbstractAdviceEventHandler<T>  implements PathProcessor<T> {

	/** The path matcher used for Ant-style pattern matching. */
	protected PathMatcher pathMatcher = new AntPathMatcher();

	/** The list of Ant-style path patterns this handler applies to. */
	protected List<String> appliedPaths = new ArrayList<String>();

	/**
	 * Adds a path pattern to the list of patterns this handler applies to.
	 *
	 * @param path an Ant-style path pattern
	 * @return this handler instance for fluent chaining
	 */
	@Override
	public DisruptorHandler<T> processPath(String path) {
		this.appliedPaths.add(path);
		return this;
	}

	/**
	 * Extracts the routing expression from the event for path matching.
	 *
	 * @param event the event to extract the path from
	 * @return the routing expression
	 */
	protected String getPathWithinEvent(T event) {
		return event.getRouteExpression();
	}

	/**
	 * Tests whether the given path pattern matches the event's routing
	 * expression.
	 *
	 * @param path  the Ant-style path pattern
	 * @param event the event to match against
	 * @return {@code true} if the pattern matches
	 */
	protected boolean pathsMatch(String path, T event) {
		String eventExp = getPathWithinEvent(event);
		log.trace("Attempting to match pattern '{}' with current Event Expression '{}'...", path, eventExp);
		return pathsMatch(path, eventExp);
	}

	/**
	 * Tests whether the given pattern matches the given path string.
	 *
	 * @param pattern the Ant-style pattern
	 * @param path    the path string
	 * @return {@code true} if the pattern matches
	 */
	protected boolean pathsMatch(String pattern, String path) {
		return pathMatcher.match(pattern, path);
	}

	/**
	 * Called before the handler chain executes. Checks the applied paths
	 * list and delegates to {@link #onPreHandle} for the first matching
	 * pattern.
	 *
	 * @param event the event being processed
	 * @return {@code true} to continue chain execution
	 * @throws Exception if an error occurs
	 */
	protected boolean preHandle(T event) throws Exception {

		if (this.appliedPaths == null || this.appliedPaths.isEmpty()) {
			if (log.isTraceEnabled()) {
				log.trace("appliedPaths property is null or empty.  This Handler will passthrough immediately.");
			}
			return true;
		}

		for (String path : this.appliedPaths) {
			// If the path does match, then pass on to the subclass
			// implementation for specific checks
			// (first match 'wins'):
			if (pathsMatch(path, event)) {
				log.trace("Current Event Expression matches pattern '{}'.  Determining handler chain execution...", path);
				return isHandlerChainContinued(event, path);
			}
		}

		// no path matched, allow the request to go through:
		return true;
	}

	private boolean isHandlerChainContinued(T event, String path) throws Exception {

		if (isEnabled(event, path)) { // isEnabled check

			if (log.isTraceEnabled()) {
				log.trace("Handler '{}' is enabled for the current event under path '{}'.  " + "Delegating to subclass implementation for 'onPreHandle' check.", new Object[] { getName(), path });
			}
			// The handler is enabled for this specific request, so delegate to
			// subclass implementations
			// so they can decide if the request should continue through the
			// chain or not:
			return onPreHandle(event);
		}

		if (log.isTraceEnabled()) {
			log.trace("Handler '{}' is disabled for the current event under path '{}'.  " + "The next element in the HandlerChain will be called immediately.", new Object[] { getName(), path });
		}
		// This handler is disabled for this specific request,
		// return 'true' immediately to indicate that the handler will not
		// process the request
		// and let the request/response to continue through the handler chain:
		return true;
	}

	/**
	 * Called when a matching path is found and the handler is enabled.
	 * Subclasses can override to add custom pre-handle logic.
	 *
	 * @param event the event being processed
	 * @return {@code true} to continue chain execution
	 * @throws Exception if an error occurs
	 */
	protected boolean onPreHandle(T event) throws Exception {
		return true;
	}

	/**
	 * Checks whether this handler is enabled for the given event and path.
	 * Defaults to the general {@link #isEnabled()} flag.
	 *
	 * @param event the event being processed
	 * @param path  the matched path pattern
	 * @return {@code true} if the handler is enabled
	 * @throws Exception if an error occurs
	 */
	protected boolean isEnabled(T event, String path) throws Exception {
		return isEnabled();
	}

	/**
	 * Returns the path matcher used for pattern matching.
	 *
	 * @return the path matcher
	 */
	public PathMatcher getPathMatcher() {
		return pathMatcher;
	}

	/**
	 * Sets the path matcher to use for pattern matching.
	 *
	 * @param pathMatcher the path matcher (must not be {@code null})
	 */
	public void setPathMatcher(PathMatcher pathMatcher) {
		this.pathMatcher = pathMatcher;
	}

	/**
	 * Returns the list of Ant-style path patterns this handler applies to.
	 *
	 * @return the applied paths list
	 */
	public List<String> getAppliedPaths() {
		return appliedPaths;
	}

}
