package se.liu.ida.hefquin.base.data.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.utils.IteratorFactory;
import se.liu.ida.hefquin.base.utils.IteratorFactoryBasedIterable;

/**
 * This is an iterator over all solution mappings contained in a nested
 * collection (more precisely, in lists contained in a collection).
 */
public class SolutionMappingsIteratorOverCollectionOfLists implements Iterator<SolutionMapping>
{
	public static Iterable<SolutionMapping> createAsIterable( final Collection<List<SolutionMapping>> c ) {
		return new IteratorFactoryBasedIterable<>( getFactory(c) );
}


	final protected Iterator<List<SolutionMapping>> itBuckets;
	protected Iterator<SolutionMapping> itCurBucketElmts;

	public SolutionMappingsIteratorOverCollectionOfLists( final Collection<List<SolutionMapping>> c ) {
		itBuckets = c.iterator();
		if ( itBuckets.hasNext() )
			itCurBucketElmts = itBuckets.next().iterator();
		else
			itCurBucketElmts = null;
	}

	@Override
	public boolean hasNext() {
		if ( itCurBucketElmts == null )
			return false;

		while ( ! itCurBucketElmts.hasNext() && itBuckets.hasNext() ) {
			itCurBucketElmts = itBuckets.next().iterator();
		}

		return itCurBucketElmts.hasNext();
	}

	@Override
	public SolutionMapping next() {
		if ( ! hasNext() )
			throw new NoSuchElementException();

		return itCurBucketElmts.next();
	}


	public static IteratorFactory<SolutionMapping> getFactory( final Collection<List<SolutionMapping>> c ) {
		return new IteratorFactory<SolutionMapping>() {
			@Override
			public Iterator<SolutionMapping> createIterator() {
				return new SolutionMappingsIteratorOverCollectionOfLists(c);
			}
		};
	}

}
