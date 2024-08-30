package se.liu.ida.hefquin.base.data.utils;

import java.util.Iterator;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.utils.WrappingIterable;
import se.liu.ida.hefquin.base.utils.WrappingIteratorFactory;

/**
 * Attention: if you need a list of all the resulting solution mappings, use
 * {@link SolutionMappingUtils#applyVocabularyMappingG2L(Iterator, VocabularyMapping)
 * instead.
 */
public class RewritingIteratorForSolMapsG2L extends RewritingIteratorForSolMapsBase
{
	public static WrappingIterable<SolutionMapping> createAsIterable( final Iterable<SolutionMapping> input,
	                                                                  final VocabularyMapping vm ) {
		return new WrappingIterable<SolutionMapping>(input, getFactory(vm) );
	}


	public RewritingIteratorForSolMapsG2L( final Iterator<SolutionMapping> input,
	                                       final VocabularyMapping vm ) {
		super(input, vm);
	}

	public RewritingIteratorForSolMapsG2L( final Iterable<SolutionMapping> input,
	                                       final VocabularyMapping vm ) {
		super(input, vm);
	}

	@Override
	protected Iterable<SolutionMapping> translate( final SolutionMapping sm ) {
		return vm.translateSolutionMappingFromGlobal(sm);
	}


	public static WrappingIteratorFactory<SolutionMapping> getFactory( final VocabularyMapping vm ) {
		return new WrappingIteratorFactory<SolutionMapping>() {
			@Override
			public Iterator<SolutionMapping> createIterator(final Iterator<SolutionMapping> input) {
				return new RewritingIteratorForSolMapsG2L(input, vm);
			}
		};
	}

}
