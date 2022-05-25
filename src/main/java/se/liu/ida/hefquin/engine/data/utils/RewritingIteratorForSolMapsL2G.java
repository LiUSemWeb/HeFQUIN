package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;

public class RewritingIteratorForSolMapsL2G extends RewritingIteratorForSolMapsBase
{
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

}
