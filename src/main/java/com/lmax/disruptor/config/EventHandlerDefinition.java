package com.lmax.disruptor.config;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class EventHandlerDefinition {

	/**
	 * 当前处理器所在位置
	 */
	private int order = 0;

	/**
	 * 处理器链定义
	 */
	private String definitions = null;

	/**
	 * 匹配规则定义
	 */
	private Map<String /* ruleExpress */, String /* handler names */> definitionMap = new LinkedHashMap<String, String>();

}
