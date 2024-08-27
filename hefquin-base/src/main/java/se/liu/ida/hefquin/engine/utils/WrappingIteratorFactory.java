package se.liu.ida.hefquin.engine.utils;

import java.util.Iterator;

public interface WrappingIteratorFactory<T>
{
	/**
	 * Creates an iterator that wraps the given iterator.
	 */
	Iterator<T> createIterator( Iterator<T> input );
}
