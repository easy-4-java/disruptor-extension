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
package com.lmax.disruptor.exception;

/**
 * Unchecked exception thrown when an error occurs during Disruptor event
 * handling. Wraps the original cause so that handler chains can propagate
 * errors without declaring checked exceptions.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see RuntimeException
 */
@SuppressWarnings("serial")
public class EventHandleException extends RuntimeException {

    /**
     * Constructs a new exception wrapping the given checked exception.
     * The original exception is preserved as the {@linkplain #getCause() cause}
     * so that handler chains and upper layers can inspect the full stack trace.
     *
     * @param e the original exception (must not be {@code null})
     */
    public EventHandleException(Exception e) {
        super(e.getMessage(), e);
    }

    /**
     * Constructs a new exception with the specified detail message and no
     * underlying cause.
     *
     * @param errorMessage the detail message
     */
    public EventHandleException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param errorMessage the detail message
     * @param cause        the underlying cause (may be {@code null})
     */
    public EventHandleException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }


}
