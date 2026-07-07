package com.lmax.disruptor.event.handler;

import lombok.extern.slf4j.Slf4j;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.chain.HandlerChain;

@Slf4j
public class AbstractAdviceEventHandler<T extends DisruptorEvent> extends AbstractEnabledEventHandler<T> {

	protected boolean preHandle(T event) throws Exception {
		return true;
	}

	protected void postHandle(T event) throws Exception {
	}

	public void afterCompletion(T event, Exception exception) throws Exception {
	}

	protected void executeChain(T event, HandlerChain<T> chain) throws Exception {
		chain.doHandler(event);
	}

	@Override
	public void doHandlerInternal(T event, HandlerChain<T> handlerChain) throws Exception {

		if (!isEnabled()) {
        	log.debug("Handler '{}' is not enabled for the current event.  Proceeding without invoking this handler.", getName());
        	// Proceed without invoking this handler...
            handlerChain.doHandler(event);
		} else {

			log.trace("Handler '{}' enabled.  Executing now.", getName());
			
			Exception exception = null;
			
			try {
	
				boolean continueChain = preHandle(event);
				if (log.isTraceEnabled()) {
					log.trace("Invoked preHandle method.  Continuing chain?: [" + continueChain + "]");
				}
				if (continueChain) {
					executeChain(event, handlerChain);
				}
				postHandle(event);
				if (log.isTraceEnabled()) {
					log.trace("Successfully invoked postHandle method");
				}
	
			} catch (Exception e) {
				exception = e;
			} finally {
				cleanup(event, exception);
			}
		}

	}

	protected void cleanup(T event, Exception existing) throws Exception {
		Exception exception = existing;
		try {
			afterCompletion(event, exception);
			if (log.isTraceEnabled()) {
				log.trace("Successfully invoked afterCompletion method.");
			}
		} catch (Exception e) {
			if (exception == null) {
				exception = e;
			} else {
				log.debug("afterCompletion implementation threw an exception.  This will be ignored to "
						+ "allow the original source exception to be propagated.", e);
			}
		}
	}

}
