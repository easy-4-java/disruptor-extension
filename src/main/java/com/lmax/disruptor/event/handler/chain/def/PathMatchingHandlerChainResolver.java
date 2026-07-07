package com.lmax.disruptor.event.handler.chain.def;

import com.lmax.disruptor.util.AntPathMatcher;
import com.lmax.disruptor.util.PathMatcher;
import lombok.extern.slf4j.Slf4j;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.chain.HandlerChain;
import com.lmax.disruptor.event.handler.chain.HandlerChainManager;
import com.lmax.disruptor.event.handler.chain.HandlerChainResolver;

@Slf4j
public class PathMatchingHandlerChainResolver implements HandlerChainResolver<DisruptorEvent> {

	/**
	 * handlerChain管理器
	 */
	private HandlerChainManager<DisruptorEvent> handlerChainManager;
	
	/**
	 * 路径匹配器
	 */
	private PathMatcher pathMatcher;
	
	 public PathMatchingHandlerChainResolver() {
        this.pathMatcher = new AntPathMatcher();
        this.handlerChainManager = new DefaultHandlerChainManager();
    }

	public HandlerChainManager<DisruptorEvent> getHandlerChainManager() {
		return handlerChainManager;
	}

	public void setHandlerChainManager(HandlerChainManager<DisruptorEvent> handlerChainManager) {
		this.handlerChainManager = handlerChainManager;
	}

	public PathMatcher getPathMatcher() {
		return pathMatcher;
	}

	public void setPathMatcher(PathMatcher pathMatcher) {
		this.pathMatcher = pathMatcher;
	}
	
	
	@Override
    public HandlerChain<DisruptorEvent> getChain(DisruptorEvent event, HandlerChain<DisruptorEvent> originalChain) {
        HandlerChainManager<DisruptorEvent> handlerChainManager = getHandlerChainManager();
        if (!handlerChainManager.hasChains()) {
            return null;
        }
        String eventURI = getPathWithinEvent(event);
        for (String pathPattern : handlerChainManager.getChainNames()) {
            if (pathMatches(pathPattern, eventURI)) {
                if (log.isTraceEnabled()) {
                    log.trace("Matched path pattern [" + pathPattern + "] for eventURI [" + eventURI + "].  " +
                            "Utilizing corresponding handler chain...");
                }
                return handlerChainManager.proxy(originalChain, pathPattern);
            }
        }
        return null;
    }

    protected boolean pathMatches(String pattern, String path) {
        PathMatcher pathMatcher = getPathMatcher();
        return pathMatcher.match(pattern, path);
    }

    protected String getPathWithinEvent(DisruptorEvent event) {
    	return event.getRouteExpression();
    }
	
}
