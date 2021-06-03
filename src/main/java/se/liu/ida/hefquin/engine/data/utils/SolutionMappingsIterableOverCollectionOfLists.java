package se.liu.ida.hefquin.engine.data.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

/**
 * This is an iterable that wraps a nested collection of solution mappings
 * (more precisely, lists of solution mappings contained in a collection)
 * and that can be used to iterate over all these solution mappings.
 */
public class SolutionMappingsIterableOverCollectionOfLists implements Iterable<SolutionMapping>
{
	protected final Collection<List<SolutionMapping>> c;

	public SolutionMappingsIterableOverCollectionOfLists( final Collection<List<SolutionMapping>> c ) {
		assert c != null;
		this.c = c;
	}

	@Override
	public Iterator<SolutionMapping> iterator() {
		return new SolutionMappingsIteratorOverCollectionOfLists(c);
	}

}
