package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.utils.WrappingIterable;
import se.liu.ida.hefquin.engine.utils.WrappingIteratorFactory;

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
	public static WrappingIterable<SolutionMapping> createAsIterable( final Iterable<SolutionMapping> input1,
	                                                                  final Iterable<SolutionMapping> input2 ) {
		return new WrappingIterable<SolutionMapping>(input1, getFactory(input2) );
	}


	protected final Iterable<SolutionMapping> input2;

	protected Iterator<SolutionMapping> it1;
	protected Iterator<SolutionMapping> it2;

	protected SolutionMapping currentInputElement;
	protected SolutionMapping nextOutputElement;

	public JoiningIteratorForSolMaps( final Iterator<SolutionMapping> input1,
	                                  final Iterable<SolutionMapping> input2 ) {
		this.input2 = input2;

		it1 = input1;
		it2 = input2.iterator();

		currentInputElement = it1.hasNext() ? it1.next() : null;
		nextOutputElement   = null;
	}

	public JoiningIteratorForSolMaps( final Iterable<SolutionMapping> input1,
	                                  final Iterable<SolutionMapping> input2 ) {
		this( input1.iterator(), input2 );
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


	public static WrappingIteratorFactory<SolutionMapping> getFactory( final Iterable<SolutionMapping> input2 ) {
		return new WrappingIteratorFactory<SolutionMapping>() {
			@Override
			public Iterator<SolutionMapping> createIterator(final Iterator<SolutionMapping> input) {
				return new JoiningIteratorForSolMaps(input, input2);
			}
		};
	}

}
