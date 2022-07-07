package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.utils.WrappingIterable;
import se.liu.ida.hefquin.engine.utils.WrappingIteratorFactory;

/**
 * Attention: if you need a list of all the resulting solution mappings, use
 * {@link SolutionMappingUtils#applyVocabularyMappingL2G(Iterator, VocabularyMapping)}
 * instead.
 */
public class RewritingIteratorForSolMapsL2G extends RewritingIteratorForSolMapsBase
{
	public static WrappingIterable<SolutionMapping> createAsIterable( final Iterable<SolutionMapping> input,
	                                                                  final VocabularyMapping vm ) {
		return new WrappingIterable<SolutionMapping>(input, getFactory(vm) );
	}


	public RewritingIteratorForSolMapsL2G( final Iterator<SolutionMapping> input,
	                                       final VocabularyMapping vm ) {
		super(input, vm);
	}

	public RewritingIteratorForSolMapsL2G( final Iterable<SolutionMapping> input,
	                                       final VocabularyMapping vm ) {
		super(input, vm);
	}

	@Override
	protected Iterable<SolutionMapping> translate( final SolutionMapping sm ) {
		return vm.translateSolutionMapping(sm);
	}


	public static WrappingIteratorFactory<SolutionMapping> getFactory( final VocabularyMapping vm ) {
		return new WrappingIteratorFactory<SolutionMapping>() {
			@Override
			public Iterator<SolutionMapping> createIterator(final Iterator<SolutionMapping> input) {
				return new RewritingIteratorForSolMapsL2G(input, vm);
			}
		};
	}

}
