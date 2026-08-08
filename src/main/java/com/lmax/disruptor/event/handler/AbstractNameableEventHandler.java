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

import com.lmax.disruptor.event.DisruptorEvent;

/**
 * Abstract base class that implements both {@link DisruptorHandler} and
 * {@link Nameable}, providing a configurable handler name.
 *
 * @param <T> the DisruptorEvent subtype handled by this handler
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see DisruptorHandler
 * @see Nameable
 */
public abstract class AbstractNameableEventHandler<T extends DisruptorEvent> implements DisruptorHandler<T>, Nameable {

	/** The configuration name of this handler. */
	protected String name;

	/**
	 * Returns the configuration name of this handler.
	 *
	 * @return the handler name
	 */
	protected String getName() {
		return this.name;
	}

	/**
	 * Sets the configuration name of this handler.
	 *
	 * @param name the handler name (must not be blank)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

}
