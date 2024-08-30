package se.liu.ida.hefquin.base.data.utils;

import java.util.Iterator;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.utils.WrappingIterable;
import se.liu.ida.hefquin.base.utils.WrappingIteratorFactory;

/**
 * This is an iterator of solution mappings that consumes another iterator
 * and passes on only the solution mappings that are compatible to a given
 * solution mapping.
 */
public class FilteringIteratorForSolMaps_CompatibleSolMap extends FilteringIteratorForSolMapsBase
{
	public static WrappingIterable<SolutionMapping> createAsIterable( final Iterable<SolutionMapping> input,
	                                                                  final SolutionMapping sm ) {
		return new WrappingIterable<SolutionMapping>(input, getFactory(sm) );
	}


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


	public static WrappingIteratorFactory<SolutionMapping> getFactory( final SolutionMapping sm ) {
		return new WrappingIteratorFactory<SolutionMapping>() {
			@Override
			public Iterator<SolutionMapping> createIterator(final Iterator<SolutionMapping> input) {
				return new FilteringIteratorForSolMaps_CompatibleSolMap(input, sm);
			}
		};
	}

}
