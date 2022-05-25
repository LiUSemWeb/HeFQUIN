package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;

public class RewritingIterableForSolMapsL2G implements Iterable<SolutionMapping>
{
	protected final Iterable<SolutionMapping> input;
	protected final VocabularyMapping vm;

	public RewritingIterableForSolMapsL2G( final Iterable<SolutionMapping> input,
	                                       final VocabularyMapping vm ) {
		assert input != null;
		assert vm != null;

		this.input = input;
		this.vm = vm;
		
	}

	@Override
	public Iterator<SolutionMapping> iterator() {
		return new RewritingIteratorForSolMapsL2G(input, vm);
	}

}
