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
package com.lmax.disruptor.thread;

import com.lmax.disruptor.*;

import java.util.function.Function;

/**
 * Enum that maps a named wait strategy to its corresponding
 * {@link WaitStrategy} instance. Provides a convenient way to select the
 * desired trade-off between CPU consumption and latency.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see WaitStrategy
 * @see BlockingWaitStrategy
 * @see SleepingWaitStrategy
 * @see YieldingWaitStrategy
 * @see BusySpinWaitStrategy
 */
public enum DisruptorWaitStrategy {

    /**
     * {@link BlockingWaitStrategy} -- least CPU-efficient but provides the
     * most consistent performance across different environments.
     */
    BLOCKING_WAIT((x) -> new BlockingWaitStrategy()),

    /**
     * {@link SleepingWaitStrategy} -- similar performance to blocking, but
     * minimises impact on producer threads. Good for async logging.
     */
    SLEEPING_WAIT((x) -> new SleepingWaitStrategy()),

    /**
     * {@link YieldingWaitStrategy} -- low-latency strategy that increases
     * CPU usage. Recommended when consumer threads &lt; logical cores.
     */
    YIELDING_WAIT((x) -> new YieldingWaitStrategy()),

    /**
     * {@link BusySpinWaitStrategy} -- highest-performance strategy.
     * Requires consumer threads &lt; physical cores (hyper-threading
     * disabled).
     */
    BUSYSPIN_WAIT((x) -> new BusySpinWaitStrategy());

    Function<Integer, com.lmax.disruptor.WaitStrategy> function;

    DisruptorWaitStrategy(Function<Integer, com.lmax.disruptor.WaitStrategy> function){
        this.function = function;
    }

    /**
     * Looks up a {@code DisruptorWaitStrategy} by name (case-insensitive).
     *
     * @param name the strategy name (e.g. "BLOCKING_WAIT")
     * @return the matching strategy, or {@code null} if not found
     */
    public static DisruptorWaitStrategy from(String name) {
        for (DisruptorWaitStrategy strategy : DisruptorWaitStrategy.values()) {
            if (strategy.name().equalsIgnoreCase(name)) {
                return strategy;
            }
        }
        return null;
    }

    /**
     * Creates and returns a new {@link WaitStrategy} instance for this
     * enum constant.
     *
     * @return a new WaitStrategy instance
     */
    public WaitStrategy get() {
        return function.apply(0);
    }

}
