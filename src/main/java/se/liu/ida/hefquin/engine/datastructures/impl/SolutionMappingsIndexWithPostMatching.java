package se.liu.ida.hefquin.engine.datastructures.impl;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingsIterableWithSolMapFilter;
import se.liu.ida.hefquin.engine.datastructures.SolutionMappingsIndex;

public class SolutionMappingsIndexWithPostMatching extends WrappingSolutionMappingsIndex
{
	public SolutionMappingsIndexWithPostMatching( final SolutionMappingsIndex wrappedIndex ) {
		super(wrappedIndex);
	}

	@Override
	public Iterable<SolutionMapping> getJoinPartners( final SolutionMapping sm ) {
		final Iterable<SolutionMapping> it = wrappedIndex.getJoinPartners(sm);
		return new SolutionMappingsIterableWithSolMapFilter(it, sm);
	}

}
