package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

/**
 * This is an iterator of solution mappings that consumes another iterator
 * and passes on only the solution mappings that are compatible to a given
 * solution mapping.
 */
public class FilteringIteratorForSolMaps_CompatibleSolMap extends FilteringIteratorForSolMapsBase
{
	protected final SolutionMapping givenSolMap;

	public FilteringIteratorForSolMaps_CompatibleSolMap( final Iterator<SolutionMapping> input, final SolutionMapping sm ) {
		super(input);

		assert sm != null;
		givenSolMap = sm;
	}

	public FilteringIteratorForSolMaps_CompatibleSolMap( final Iterable<SolutionMapping> input, final SolutionMapping sm ) {
		this( input.iterator(), sm );
	}

	@Override
	protected SolutionMapping applyFilter( final SolutionMapping sm ) {
		return SolutionMappingUtils.compatible(givenSolMap, sm) ? sm : null;
	}

}
