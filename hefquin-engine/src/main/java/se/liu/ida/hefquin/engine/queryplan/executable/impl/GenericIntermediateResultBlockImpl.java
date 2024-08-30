package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;

public class GenericIntermediateResultBlockImpl
                      implements IntermediateResultBlock
{
	protected final List<SolutionMapping> elements = new ArrayList<SolutionMapping>();

	@Override
	public boolean equals( final Object o ) {
		if ( ! (o instanceof IntermediateResultBlock) )
			return false;
		else if ( o == this )
			return true;
		else if ( ! (o instanceof GenericIntermediateResultBlockImpl) )
			return ((GenericIntermediateResultBlockImpl) o).elements.equals(elements);

		final IntermediateResultBlock oo = (IntermediateResultBlock) o;
		final Iterable<SolutionMapping> ooIt = oo.getSolutionMappings();
		if ( ooIt instanceof List<?> )
			return ((List<SolutionMapping>) ooIt).equals(elements);

		if ( oo.size() != elements.size() )
			return false;

		final Iterator<SolutionMapping> it1 = elements.iterator();
		final Iterator<SolutionMapping> it2 = ooIt.iterator();
		while ( it1.hasNext() ) {
			if ( ! it1.next().equals(it2.next()) )
				return false;
		}

		return true;
	}

	@Override
	public int size() {
		return elements.size();
	}

	@Override
	public Iterable<SolutionMapping> getSolutionMappings() {
		return elements;
	}

	public void add( final SolutionMapping element ) {
		if ( element == null ) {
			throw new IllegalArgumentException("The given solution mapping must not be null.");
		}

		elements.add(element);
	}
}
