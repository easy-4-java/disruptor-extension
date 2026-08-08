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
package com.lmax.disruptor.util;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;

/**
 * Utility class that provides pre-configured singleton instances of each
 * built-in {@link WaitStrategy} offered by the LMAX Disruptor.
 *
 * <p>These constants allow callers to select a wait strategy by name
 * without constructing new objects each time.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see WaitStrategy
 * @see DisruptorWaitStrategy
 */
public class WaitStrategys {

    /**
     * {@link BlockingWaitStrategy} is the least CPU-efficient strategy but
     * consumes the fewest CPU resources and provides more consistent
     * performance across different deployment environments.
     */
    public static WaitStrategy BLOCKING_WAIT = new BlockingWaitStrategy();

    /**
     * {@link SleepingWaitStrategy} has performance similar to
     * {@link BlockingWaitStrategy} with comparable CPU consumption, but
     * minimizes impact on producer threads. Suitable for asynchronous
     * logging scenarios.
     */
    public static WaitStrategy SLEEPING_WAIT = new SleepingWaitStrategy();

    /**
     * {@link YieldingWaitStrategy} is one of the two strategies that can be
     * used in low-latency systems. It reduces system latency at the cost of
     * increased CPU usage. Recommended when event consumer threads are fewer
     * than logical cores (e.g. with hyper-threading enabled).
     */
    public static WaitStrategy YIELDING_WAIT = new YieldingWaitStrategy();

    /**
     * {@link BusySpinWaitStrategy} is the highest-performance strategy and
     * also the most demanding on the deployment environment. Best used when
     * event processing threads are fewer than physical cores (e.g. with
     * hyper-threading disabled).
     */
    public static WaitStrategy BUSYSPIN_WAIT = new BusySpinWaitStrategy();

}
