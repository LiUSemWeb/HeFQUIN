package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

public class SolutionMappingsIteratorWithSolMapFilter implements Iterator<SolutionMapping>
{
	protected final Iterator<SolutionMapping> input;
	protected final SolutionMapping sm;

	protected SolutionMapping nextOutputElement = null;

	public SolutionMappingsIteratorWithSolMapFilter( final Iterator<SolutionMapping> input, final SolutionMapping sm ) {
		this.input = input;
		this.sm = sm;
	}

	@Override
	public boolean hasNext() {
		while ( nextOutputElement == null && input.hasNext() ) {
			final SolutionMapping nextInputElement = input.next();
			if ( SolutionMappingUtils.compatible(sm, nextInputElement) ) {
				nextOutputElement = nextInputElement;
			}
		}

		return ( nextOutputElement != null );
	}

	@Override
	public SolutionMapping next() {
		if ( ! hasNext() )
			throw new NoSuchElementException();

		return nextOutputElement;
	};

}
