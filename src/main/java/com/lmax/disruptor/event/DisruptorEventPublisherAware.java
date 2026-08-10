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
 * Interface to be implemented by any component that wishes to be notified
 * of the {@link DisruptorEventPublisher} it runs in.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see DisruptorEventPublisher
 */
public interface DisruptorEventPublisherAware {

	/**
	 * Sets the {@link DisruptorEventPublisher} that the implementing object
	 * runs in.
	 *
	 * @param disruptorEventPublisher the publisher to inject (never
	 *                                {@code null})
	 */
	void setDisruptorEventPublisher(DisruptorEventPublisher disruptorEventPublisher);

}
