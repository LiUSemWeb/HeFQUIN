package se.liu.ida.hefquin.engine.utils;

import java.util.Iterator;

public interface IteratorFactory<T>
{
	/**
	 * Creates an iterator.
	 */
	Iterator<T> createIterator();
}
