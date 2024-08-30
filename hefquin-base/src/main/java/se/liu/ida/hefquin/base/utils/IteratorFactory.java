package se.liu.ida.hefquin.base.utils;

import java.util.Iterator;

public interface IteratorFactory<T>
{
	/**
	 * Creates an iterator.
	 */
	Iterator<T> createIterator();
}
