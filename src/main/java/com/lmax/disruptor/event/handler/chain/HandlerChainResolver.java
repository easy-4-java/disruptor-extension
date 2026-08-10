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

import com.lmax.disruptor.event.DisruptorEvent;

/**
 * Strategy interface for resolving which {@link HandlerChain} should
 * process a given event. Implementations typically use the event's routing
 * expression to select the appropriate chain.
 *
 * @param <T> the DisruptorEvent subtype
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see HandlerChain
 * @see com.lmax.disruptor.event.handler.chain.def.PathMatchingHandlerChainResolver
 */
public interface HandlerChainResolver<T extends DisruptorEvent> {

	/**
	 * Resolves and returns the handler chain for the given event, wrapping
	 * the original chain if necessary. Returns {@code null} if no
	 * configured chain matches the event.
	 *
	 * @param event         the event to resolve the chain for
	 * @param originalChain the default handler chain
	 * @return the resolved handler chain, or {@code null}
	 */
	HandlerChain<T> getChain(T event , HandlerChain<T> originalChain);

}
