package se.liu.ida.hefquin.base.data.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.data.SolutionMapping;

public abstract class FilteringIteratorForSolMapsBase implements Iterator<SolutionMapping>
{
	protected final Iterator<SolutionMapping> input;

	protected SolutionMapping nextOutputElement = null;

	protected FilteringIteratorForSolMapsBase( final Iterator<SolutionMapping> input ) {
		assert input != null;
		this.input = input;
	}

	protected FilteringIteratorForSolMapsBase( final Iterable<SolutionMapping> input ) {
		this( input.iterator() );
	}

	@Override
	public boolean hasNext() {
		while ( nextOutputElement == null && input.hasNext() ) {
			nextOutputElement = applyFilter( input.next() );
		}

		return ( nextOutputElement != null );
	}

	@Override
	public SolutionMapping next() {
		if ( ! hasNext() )
			throw new NoSuchElementException();

		final SolutionMapping output = nextOutputElement;
		nextOutputElement = null;
		return output;
	}

	/**
	 * Returns the given solution mapping if it passes the filter condition
	 * checked by the subclass. Returns <code>null</code> if the given
	 * solution mapping does not pass the filter condition.
	 */
	protected abstract SolutionMapping applyFilter(SolutionMapping sm);

}
