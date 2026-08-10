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
package com.lmax.disruptor.event;

/**
 * Strategy interface for publishing a {@link DisruptorEvent} to the LMAX
 * Disruptor ring buffer.
 *
 * <p>Implementations typically delegate to
 * {@link com.lmax.disruptor.dsl.Disruptor#publishEvent}.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see DisruptorEvent
 * @see DisruptorEventPublisherAware
 */
public interface DisruptorEventPublisher {

	/**
	 * Publishes the given event to the Disruptor ring buffer.
	 *
	 * @param event the event to publish (must not be {@code null})
	 */
	void publishEvent(DisruptorEvent event);

}
