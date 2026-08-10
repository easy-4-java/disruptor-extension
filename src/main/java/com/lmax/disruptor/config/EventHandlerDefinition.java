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
package com.lmax.disruptor.config;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration definition that describes a set of event handler chains
 * keyed by routing-rule expressions.
 *
 * <p>Used by the INI-based configuration mechanism to map Ant-style
 * event patterns to comma-separated lists of handler names.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see Ini
 * @see com.lmax.disruptor.annotation.EventRule
 */
@Data
public class EventHandlerDefinition {

	/**
	 * The ordinal position of this handler definition within the processing
	 * pipeline. Lower values execute earlier.
	 */
	private int order = 0;

	/**
	 * Raw definition string (comma-separated handler names).
	 */
	private String definitions = null;

	/**
	 * Map from Ant-style routing-rule expressions to comma-separated handler
	 * names. The key is the rule expression; the value is the handler chain
	 * definition.
	 */
	private Map<String /* ruleExpress */, String /* handler names */> definitionMap = new LinkedHashMap<String, String>();

}
