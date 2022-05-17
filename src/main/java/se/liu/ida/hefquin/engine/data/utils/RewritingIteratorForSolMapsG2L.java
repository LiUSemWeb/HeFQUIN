package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;

/**
 * Attention: if you need a list of all the resulting solution mappings, use
 * {@link SolutionMappingUtils#applyVocabularyMappingG2L(Iterator, VocabularyMapping)
 * instead.
 */
public class RewritingIteratorForSolMapsG2L implements Iterator<SolutionMapping>
{
	protected final Iterator<SolutionMapping> input;
	protected final VocabularyMapping vm;

	protected Iterator<SolutionMapping> currentOutput = null;

	public RewritingIteratorForSolMapsG2L( final Iterator<SolutionMapping> input,
	                                       final VocabularyMapping vm ) {
		assert input != null;
		assert vm != null;

		this.input = input;
		this.vm = vm;
	}

	public RewritingIteratorForSolMapsG2L( final Iterable<SolutionMapping> input,
	                                       final VocabularyMapping vm ) {
		this( input.iterator(), vm );
	}

	@Override
	public boolean hasNext() {
		while ( (currentOutput == null || ! currentOutput.hasNext()) && input.hasNext() ) {
			final SolutionMapping nextSM = input.next();
			final Iterable<SolutionMapping> nextOutput = vm.translateSolutionMappingFromGlobal(nextSM);
			currentOutput = nextOutput.iterator();
		}

		return ( currentOutput != null && currentOutput.hasNext() );
	}

	@Override
	public SolutionMapping next() {
		if ( ! hasNext() )
			throw new NoSuchElementException();

		return currentOutput.next();
	}

}
