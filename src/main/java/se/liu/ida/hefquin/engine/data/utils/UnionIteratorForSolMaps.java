package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

/**
 * This iterator enumerates the solution mappings from two other iterators,
 * essentially producing a union of the collections of solution mappings
 * that the other two iterators enumerate.
 */
public class UnionIteratorForSolMaps implements Iterator<SolutionMapping>
{
	protected final Iterator<SolutionMapping> input2;

	protected Iterator<SolutionMapping> currentInput;
	protected boolean firstInputExhausted = false;

	public UnionIteratorForSolMaps( final Iterable<SolutionMapping> input1,
	                                final Iterable<SolutionMapping> input2 ) {
		this( input1.iterator(), input2.iterator() );
	}

	public UnionIteratorForSolMaps( final Iterator<SolutionMapping> input1,
	                                final Iterator<SolutionMapping> input2 ) {
		this.input2 = input2;

		currentInput = input1;
		if ( currentInput.hasNext() ) {
			firstInputExhausted = false;
		}
		else {
			currentInput = input2;
			firstInputExhausted = true;
		}
	}

	@Override
	public boolean hasNext() {
		if ( ! currentInput.hasNext() && ! firstInputExhausted ) {
			currentInput = input2;
			firstInputExhausted = true;
		}

		return currentInput.hasNext();
	}

	@Override
	public SolutionMapping next() {
		if ( ! hasNext() ) {
			throw new NoSuchElementException();
		}

		return currentInput.next();
	}

}
