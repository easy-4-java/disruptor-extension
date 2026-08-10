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
package com.lmax.disruptor.event.handler.chain.def;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.StringUtils;

import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.DisruptorHandler;
import com.lmax.disruptor.event.handler.NamedHandlerList;
import com.lmax.disruptor.event.handler.chain.HandlerChain;
import com.lmax.disruptor.event.handler.chain.ProxiedHandlerChain;

/**
 * Default {@link NamedHandlerList} implementation backed by an
 * {@link ArrayList}. Supports creating a {@link ProxiedHandlerChain}
 * from the handlers in this list.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see NamedHandlerList
 * @see DefaultHandlerChainManager
 */
public class DefaultNamedHandlerList implements NamedHandlerList<DisruptorEvent> {

	private String name;

	private List<DisruptorHandler<DisruptorEvent>> backingList;

	/**
	 * Creates a new empty handler list with the given name.
	 *
	 * @param name the unique name for this list (must not be blank)
	 */
	public DefaultNamedHandlerList(String name) {
		 this(name, new ArrayList<DisruptorHandler<DisruptorEvent>>());
	}

	/**
	 * Creates a new handler list with the given name and backing list.
	 *
	 * @param name         the unique name for this list
	 * @param backingList  the backing list of handlers (must not be
	 *                     {@code null})
	 * @throws NullPointerException if backingList is {@code null}
	 */
	public DefaultNamedHandlerList(String name, List<DisruptorHandler<DisruptorEvent>> backingList) {
		 if (backingList == null) {
	            throw new NullPointerException("backingList constructor argument cannot be null.");
        }
        this.backingList = backingList;
        setName(name);
	}

	/**
	 * Sets the name of this handler list.
	 *
	 * @param name the name (must not be blank)
	 * @throws IllegalArgumentException if name is blank
	 */
	public void setName(String name) {
		 if (StringUtils.isBlank(name)) {
	         throw new IllegalArgumentException("Cannot specify a null or empty name.");
        }
        this.name = name;
	}

	/**
	 * Returns the configuration-unique name of this handler list.
	 *
	 * @return the handler list name
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Creates a {@link ProxiedHandlerChain} that first executes the
	 * handlers in this list and then delegates to the given chain.
	 *
	 * @param handlerChain the chain to delegate to
	 * @return a new proxied handler chain
	 */
	@Override
	public HandlerChain<DisruptorEvent> proxy(HandlerChain<DisruptorEvent> handlerChain) {
		return new ProxiedHandlerChain((ProxiedHandlerChain) handlerChain, this);
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return this.backingList.size();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return this.backingList.isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(Object o) {
		return this.backingList.contains(o);
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<DisruptorHandler<DisruptorEvent>> iterator() {
		return this.backingList.iterator();
	}

	/** {@inheritDoc} */
	@Override
	public Object[] toArray() {
		return this.backingList.toArray();
	}

	/** {@inheritDoc} */
	@Override
	public <T> T[] toArray(T[] a) {
		return this.backingList.toArray(a);
	}

	/** {@inheritDoc} */
	@Override
	public boolean add(DisruptorHandler<DisruptorEvent> e) {
		return this.backingList.add(e);
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(Object o) {
		return this.backingList.remove(o);
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsAll(Collection<?> c) {
		return this.backingList.containsAll(c);
	}

	/** {@inheritDoc} */
	@Override
	public boolean addAll(Collection<? extends DisruptorHandler<DisruptorEvent>> c) {
		return this.backingList.addAll(c);
	}

	/** {@inheritDoc} */
	@Override
	public boolean addAll(int index, Collection<? extends DisruptorHandler<DisruptorEvent>> c) {
		return this.backingList.addAll(index, c);
	}

	/** {@inheritDoc} */
	@Override
	public boolean removeAll(Collection<?> c) {
		return this.backingList.removeAll(c);
	}

	/** {@inheritDoc} */
	@Override
	public boolean retainAll(Collection<?> c) {
		return this.backingList.retainAll(c);
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		this.backingList.clear();
	}

	/** {@inheritDoc} */
	@Override
	public DisruptorHandler<DisruptorEvent> get(int index) {
		return this.backingList.get(index);
	}

	/** {@inheritDoc} */
	@Override
	public DisruptorHandler<DisruptorEvent> set(int index, DisruptorHandler<DisruptorEvent> element) {
		return this.backingList.set(index, element);
	}

	/** {@inheritDoc} */
	@Override
	public void add(int index, DisruptorHandler<DisruptorEvent> element) {
		this.backingList.add(index, element);
	}

	/** {@inheritDoc} */
	@Override
	public DisruptorHandler<DisruptorEvent> remove(int index) {
		return this.backingList.remove(index);
	}

	/** {@inheritDoc} */
	@Override
	public int indexOf(Object o) {
		return this.backingList.indexOf(o);
	}

	/** {@inheritDoc} */
	@Override
	public int lastIndexOf(Object o) {
		return this.backingList.lastIndexOf(o);
	}

	/** {@inheritDoc} */
	@Override
	public ListIterator<DisruptorHandler<DisruptorEvent>> listIterator() {
		return this.backingList.listIterator();
	}

	/** {@inheritDoc} */
	@Override
	public ListIterator<DisruptorHandler<DisruptorEvent>> listIterator(int index) {
		return this.backingList.listIterator(index);
	}

	/** {@inheritDoc} */
	@Override
	public List<DisruptorHandler<DisruptorEvent>> subList(int fromIndex, int toIndex) {
		return this.backingList.subList(fromIndex, toIndex);
	}

 }
