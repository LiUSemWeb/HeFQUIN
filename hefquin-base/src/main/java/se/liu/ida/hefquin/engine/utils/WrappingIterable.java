package se.liu.ida.hefquin.engine.utils;

import java.util.Iterator;

/**
 * Wraps another iterable over the same type and can
 * be used to apply a corresponding wrapping iterator.
 */
public class WrappingIterable<T> implements Iterable<T>
{
	protected final Iterable<T> input;
	protected final WrappingIteratorFactory<T> itFactory;

	public WrappingIterable( final Iterable<T> input,
	                         final WrappingIteratorFactory<T> itFactory ) {
		assert input != null;
		assert itFactory != null;

		this.input = input;
		this.itFactory = itFactory;
	}

	@Override
	public Iterator<T> iterator() {
		return itFactory.createIterator( input.iterator() );
	}

}
