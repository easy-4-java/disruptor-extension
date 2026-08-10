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
package com.lmax.disruptor.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that declares an Ant-style event routing rule on a handler class.
 *
 * <p>The value is an expression in the format {@code /namespace/topic/tag},
 * supporting wildcards ({@code *} and {@code **}). For example:
 * {@code /Event-DC-Output/TagA-Output/**}.</p>
 *
 * <p>This annotation is inherited, so subclasses automatically inherit the
 * routing rule of their parent class.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see com.lmax.disruptor.util.AntPathMatcher
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EventRule {

	/**
	 * Ant-style event routing expression in the format
	 * {@code /namespace/topic/tag}. Defaults to {@code "*"} (match all).
	 *
	 * @return the routing rule expression
	 */
	String value() default "*";

}
