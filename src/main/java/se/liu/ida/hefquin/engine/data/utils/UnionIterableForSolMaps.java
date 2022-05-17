package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

/**
 * This is an iterable of solution mappings that enumerates the solution
 * mappings from two other iterables, essentially producing a union of the
 * collections of solution mappings that the other two iterables enumerate.
 */
public class UnionIterableForSolMaps implements Iterable<SolutionMapping>
{
	protected final Iterable<SolutionMapping> input1;
	protected final Iterable<SolutionMapping> input2;

	public UnionIterableForSolMaps( final Iterable<SolutionMapping> input1,
	                                final Iterable<SolutionMapping> input2 ) {
		assert input1 != null;
		assert input2 != null;

		this.input1 = input1;
		this.input2 = input2;
	}

	@Override
	public Iterator<SolutionMapping> iterator() {
		return new UnionIteratorForSolMaps(input1, input2);
	}

}
