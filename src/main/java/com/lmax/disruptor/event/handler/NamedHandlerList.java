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

import java.util.List;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.chain.HandlerChain;

/**
 * A named, ordered list of {@link DisruptorHandler} instances that can be
 * proxied into a {@link HandlerChain}. The name uniquely identifies the
 * handler list within the handler chain manager.
 *
 * @param <T> the DisruptorEvent subtype
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see DisruptorHandler
 * @see HandlerChain
 */
public interface NamedHandlerList<T extends DisruptorEvent> extends List<DisruptorHandler<T>> {

	/**
	 * Returns the configuration-unique name assigned to this handler list.
	 *
	 * @return the handler list name
	 */
    String getName();

    /**
     * Returns a new {@link HandlerChain} that first executes this list's
     * handlers (in list order) and then delegates to the given handler
     * chain.
     *
     * @param handlerChain the chain to delegate to after this list
     * @return a new proxied handler chain
     */
    HandlerChain<T> proxy(HandlerChain<T> handlerChain);

}
