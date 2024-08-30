package se.liu.ida.hefquin.base.utils;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;

public class ConcatenatingIterable<T> implements Iterable<T>
{
	private final Iterable<T> it1;
	private final Iterable<T> it2;

	public ConcatenatingIterable( final Iterable<T> it1, final Iterable<T> it2 ) {
		assert it1 != null;
		assert it2 != null;

		this.it1 = it1;
		this.it2 = it2;
	} 

	@Override
	public Iterator<T> iterator() {
		return Iter.concat( it1.iterator(), it2.iterator() );
	}

}
