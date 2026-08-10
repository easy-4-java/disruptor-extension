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

import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.function.BiFunction;

/**
 * Enum-based {@link ThreadFactory} providing commonly used thread factory
 * configurations for the Disruptor framework.
 *
 * <ul>
 *   <li>{@link #DEFAULT_THREAD_FACTORY} -- daemon threads named
 *       "Disruptor-Thread"</li>
 *   <li>{@link #LOGGER_THREAD_FACTORY} -- daemon threads with SLF4J-based
 *       uncaught-exception logging</li>
 *   <li>{@link #MAX_PRIORITY_THREAD_FACTORY} -- daemon threads at
 *       {@link Thread#MAX_PRIORITY} named "Disruptor-Max-Priority-Thread"</li>
 * </ul>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see ThreadFactory
 * @see DisruptorWaitStrategy
 */
public enum DisruptorThreadFactory implements ThreadFactory {

    /** Default daemon thread factory. */
    DEFAULT_THREAD_FACTORY(true, (daemon, r) -> {
        Thread t = new Thread(r);
        t.setDaemon(daemon);
        t.setName("Disruptor-Thread");
        return t;
    }),

    /** Daemon thread factory that logs uncaught exceptions via SLF4J. */
    LOGGER_THREAD_FACTORY(true, (daemon, r) -> {
        Thread t = new Thread(r);
        t.setDaemon(daemon);
        t.setName("Disruptor-Thread");
        t.setUncaughtExceptionHandler((t1, e) -> LoggerFactory.getLogger(t1.getName()).error(e.getMessage(), e));
        return t;
    }),

    /** Daemon thread factory that creates max-priority threads. */
    MAX_PRIORITY_THREAD_FACTORY(true, (daemon, r) -> {
        Thread t = new Thread(r);
        t.setDaemon(daemon);
        t.setName("Disruptor-Max-Priority-Thread");
        t.setPriority(Thread.MAX_PRIORITY);
        return t;
    }),

    ;

    private boolean daemon = false;
    private BiFunction<Boolean, Runnable, Thread> function;


    DisruptorThreadFactory(BiFunction<Boolean, Runnable, Thread> function){
        this.function = function;
    }

    DisruptorThreadFactory(boolean daemon, BiFunction<Boolean, Runnable, Thread> function){
        this.daemon = daemon;
        this.function = function;
    }

    /**
     * Creates a new {@link Thread} using the configured factory function.
     *
     * @param r the runnable to execute
     * @return a new thread
     */
    @Override
    public Thread newThread(Runnable r) {
        return function.apply(daemon, r);
    }

}
