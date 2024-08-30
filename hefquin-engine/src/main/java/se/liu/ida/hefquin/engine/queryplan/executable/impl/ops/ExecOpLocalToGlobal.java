package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.data.utils.RewritingIteratorForSolMapsL2G;

public class ExecOpLocalToGlobal extends UnaryExecutableOpBaseWithIterator
{
	protected final VocabularyMapping vm;

	public ExecOpLocalToGlobal( final VocabularyMapping vm, final boolean collectExceptions ) {
		super(collectExceptions);

		assert vm != null;
		this.vm = vm;
	}

	@Override
	protected Iterator<SolutionMapping> createInputToOutputIterator( final Iterable<SolutionMapping> input ) {
		return new RewritingIteratorForSolMapsL2G(input, vm);
	}

}
