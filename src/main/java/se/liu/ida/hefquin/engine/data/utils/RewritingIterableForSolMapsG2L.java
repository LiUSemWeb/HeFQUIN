package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;

/**
 * Attention: if you need a list of all the resulting solution mappings, use
 * {@link SolutionMappingUtils#applyVocabularyMappingG2L(Iterator, VocabularyMapping)
 * instead.
 */
public class RewritingIterableForSolMapsG2L implements Iterable<SolutionMapping>
{
	protected final Iterable<SolutionMapping> input;
	protected final VocabularyMapping vm;

	public RewritingIterableForSolMapsG2L( final Iterable<SolutionMapping> input,
	                                       final VocabularyMapping vm ) {
		assert input != null;
		assert vm != null;

		this.input = input;
		this.vm = vm;
		
	}

	@Override
	public Iterator<SolutionMapping> iterator() {
		return new RewritingIteratorForSolMapsG2L(input, vm);
	}

}
