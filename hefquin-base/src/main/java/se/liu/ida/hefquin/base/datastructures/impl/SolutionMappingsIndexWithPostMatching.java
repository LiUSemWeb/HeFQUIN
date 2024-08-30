package se.liu.ida.hefquin.base.datastructures.impl;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.FilteringIteratorForSolMaps_CompatibleSolMap;
import se.liu.ida.hefquin.base.datastructures.SolutionMappingsIndex;

public class SolutionMappingsIndexWithPostMatching extends WrappingSolutionMappingsIndex
{
	public SolutionMappingsIndexWithPostMatching( final SolutionMappingsIndex wrappedIndex ) {
		super(wrappedIndex);
	}

	@Override
	public Iterable<SolutionMapping> getJoinPartners( final SolutionMapping sm ) {
		final Iterable<SolutionMapping> it = wrappedIndex.getJoinPartners(sm);
		return FilteringIteratorForSolMaps_CompatibleSolMap.createAsIterable(it, sm);
	}

}
