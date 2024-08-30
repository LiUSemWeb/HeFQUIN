package se.liu.ida.hefquin.base.data.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.VocabularyMapping;

/**
 * Abstract base class for classes that implement iterators that translate
 * solution mappings from an input iterator by applying a vocabulary mapping.
 */
public abstract class RewritingIteratorForSolMapsBase implements Iterator<SolutionMapping>
{
	protected final Iterator<SolutionMapping> input;
	protected final VocabularyMapping vm;

	protected Iterator<SolutionMapping> currentOutput = null;

	protected RewritingIteratorForSolMapsBase( final Iterator<SolutionMapping> input,
	                                           final VocabularyMapping vm ) {
		assert input != null;
		assert vm != null;

		this.input = input;
		this.vm = vm;
	}

	protected RewritingIteratorForSolMapsBase( final Iterable<SolutionMapping> input,
	                                           final VocabularyMapping vm ) {
		this( input.iterator(), vm );
	}

	@Override
	public boolean hasNext() {
		while ( (currentOutput == null || ! currentOutput.hasNext()) && input.hasNext() ) {
			final SolutionMapping nextSM = input.next();
			final Iterable<SolutionMapping> nextOutput = translate(nextSM);
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

	protected abstract Iterable<SolutionMapping> translate( SolutionMapping sm );

}
