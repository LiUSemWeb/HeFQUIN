package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.data.utils.RewritingIteratorForSolMapsG2L;

public class ExecOpGlobalToLocal extends UnaryExecutableOpBaseWithIterator
{
	protected final VocabularyMapping vm;

	public ExecOpGlobalToLocal( final VocabularyMapping vm, final boolean collectExceptions ) {
		super(collectExceptions);

		assert vm != null;
		this.vm = vm;
	}

	@Override
	protected Iterator<SolutionMapping> createInputToOutputIterator( final Iterable<SolutionMapping> input ) {
		return new RewritingIteratorForSolMapsG2L(input, vm);
	}

}
