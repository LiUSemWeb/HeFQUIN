package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

/**
 * This is an iterator of solution mappings that consumes another iterator
 * and passes on only the solution mappings that are compatible to a given
 * solution mapping.
 */
public class SolutionMappingsIteratorWithSolMapFilter extends FilteringIteratorForSolMapsBase
{
	protected final SolutionMapping givenSolMap;

	public SolutionMappingsIteratorWithSolMapFilter( final Iterator<SolutionMapping> input, final SolutionMapping sm ) {
		super(input);

		assert sm != null;
		givenSolMap = sm;
	}

	@Override
	protected SolutionMapping applyFilter( final SolutionMapping sm ) {
		return SolutionMappingUtils.compatible(givenSolMap, sm) ? sm : null;
	}

}
