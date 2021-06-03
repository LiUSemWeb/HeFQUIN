package se.liu.ida.hefquin.engine.data.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

public class SolutionMappingsIteratorOverCollectionOfLists implements Iterator<SolutionMapping>
{
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

}
