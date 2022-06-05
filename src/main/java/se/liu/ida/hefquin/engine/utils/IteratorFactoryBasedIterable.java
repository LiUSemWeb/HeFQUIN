package se.liu.ida.hefquin.engine.utils;

import java.util.Iterator;

/**
 * Provides iterators created by a given {@link IteratorFactory}.
 * Can be used to create iterables for specific types of iterators.
 */
public class IteratorFactoryBasedIterable<T> implements Iterable<T>
{
	protected final IteratorFactory<T> itFactory;

	public IteratorFactoryBasedIterable( final IteratorFactory<T> itFactory ) {
		assert itFactory != null;
		this.itFactory = itFactory;
	}

	@Override
	public Iterator<T> iterator() {
		return itFactory.createIterator();
	}

}
