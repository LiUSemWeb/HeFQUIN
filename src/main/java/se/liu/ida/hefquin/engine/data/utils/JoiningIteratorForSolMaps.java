package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

/**
 * This iterator enumerates the result of joining two collections of solution
 * mappings. The current implementation of this iterator performs a nested
 * loops join.
 *
 * If you need the join result materialized, use
 * {@link SolutionMappingUtils#nestedLoopJoin(Iterable, Iterable)} instead.
 */
public class JoiningIteratorForSolMaps implements Iterator<SolutionMapping>
{
	protected final Iterable<SolutionMapping> input2;

	protected Iterator<SolutionMapping> it1;
	protected Iterator<SolutionMapping> it2;

	protected SolutionMapping currentInputElement;
	protected SolutionMapping nextOutputElement;

	public JoiningIteratorForSolMaps( final Iterable<SolutionMapping> input1,
	                                  final Iterable<SolutionMapping> input2 ) {
		this.input2 = input2;

		it1 = input1.iterator();
		it2 = input2.iterator();

		currentInputElement = it1.hasNext() ? it1.next() : null;
		nextOutputElement   = null;
	}

	@Override
	public boolean hasNext() {
		while ( nextOutputElement == null && currentInputElement != null ) {
			if ( it2.hasNext() ) {
				final SolutionMapping s = it2.next();  // advance in the inner loop
				if ( SolutionMappingUtils.compatible(s, currentInputElement) ) {
					nextOutputElement = SolutionMappingUtils.merge(s, currentInputElement);
				}
			}
			else if ( it1.hasNext() ) {
				currentInputElement = it1.next();  // advance in the outer loop
				it2 = input2.iterator();           // and restart the inner loop
			}
			else {
				currentInputElement = null; // prepare to terminate
			}
		}

		return ( nextOutputElement != null );
	}

	@Override
	public SolutionMapping next() {
		if ( ! hasNext() ) {
			throw new NoSuchElementException();
		}

		final SolutionMapping r = nextOutputElement;
		nextOutputElement = null;
		return r;
	}

}
