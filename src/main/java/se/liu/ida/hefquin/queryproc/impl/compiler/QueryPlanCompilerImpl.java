package se.liu.ida.hefquin.queryproc.impl.compiler;

import se.liu.ida.hefquin.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.queryplan.ExecutablePlan;
import se.liu.ida.hefquin.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlockBuilder;
import se.liu.ida.hefquin.queryplan.executable.impl.GenericIntermediateResultBlockBuilderImpl;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.BinaryExecutableOp;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.NullaryExecutableOp;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.UnaryExecutableOp;
import se.liu.ida.hefquin.queryplan.executable.impl.pullbased.ResultBlockIterOverResultElementIter;
import se.liu.ida.hefquin.queryplan.executable.impl.pullbased.ResultBlockIterator;
import se.liu.ida.hefquin.queryplan.executable.impl.pullbased.ResultElementIterWithBinaryExecOp;
import se.liu.ida.hefquin.queryplan.executable.impl.pullbased.ResultElementIterWithNullaryExecOp;
import se.liu.ida.hefquin.queryplan.executable.impl.pullbased.ResultElementIterWithUnaryExecOp;
import se.liu.ida.hefquin.queryplan.executable.impl.pullbased.ResultElementIterator;
import se.liu.ida.hefquin.queryproc.ExecutionContext;
import se.liu.ida.hefquin.queryproc.QueryPlanCompiler;

public class QueryPlanCompilerImpl implements QueryPlanCompiler
{
	@Override
	public ExecutablePlan compile( final PhysicalPlan qep ) {
		final ExecutionContext execCxt = createExecContext();
		final ResultElementIterator it = compile( qep.getRootOperator(), execCxt );
		final ExecutableOperator execOp = new ExecutableRootOperator(it);
		return new ExecutablePlanImpl(execOp);
	}

	protected ResultElementIterator compile( final PhysicalOperator rootOp,
	                                         final ExecutionContext execCxt )
	{
		final ExecutableOperator execOp = rootOp.createExecOp();

		if ( rootOp.numberOfChildren() == 0 )
		{
			if ( ! (execOp  instanceof NullaryExecutableOp) )
				throw new IllegalArgumentException();

			final NullaryExecutableOp execOp0 = (NullaryExecutableOp) execOp;
			return new ResultElementIterWithNullaryExecOp(execOp0, execCxt);
		}
		else if ( rootOp.numberOfChildren() == 1 )
		{
			if ( ! (execOp  instanceof UnaryExecutableOp) )
				throw new IllegalArgumentException();

			final ResultElementIterator elmtIterChild = compile(rootOp.getChild(0), execCxt);
			final ResultBlockIterator blockIterChild = createBlockIterator(elmtIterChild);

			final UnaryExecutableOp execOp1 = (UnaryExecutableOp) execOp;
			return new ResultElementIterWithUnaryExecOp(execOp1, blockIterChild, execCxt);
		}
		else if ( rootOp.numberOfChildren() == 2 )
		{
			if ( ! (execOp  instanceof BinaryExecutableOp) )
				throw new IllegalArgumentException();

			final ResultElementIterator elmtIterChild1 = compile(rootOp.getChild(0), execCxt);
			final ResultBlockIterator blockIterChild1 = createBlockIterator(elmtIterChild1);

			final ResultElementIterator elmtIterChild2 = compile(rootOp.getChild(0), execCxt);
			final ResultBlockIterator blockIterChild2 = createBlockIterator(elmtIterChild2);

			final BinaryExecutableOp execOp2 = (BinaryExecutableOp) execOp;
			return new ResultElementIterWithBinaryExecOp(execOp2, blockIterChild1, blockIterChild2, execCxt);
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}

	protected ExecutionContext createExecContext() {
		return null;
		// TODO: implement createExecContext()
	}

	protected ResultBlockIterator createBlockIterator( final ResultElementIterator elmtIter ) {
		final IntermediateResultBlockBuilder blockBuilder = new GenericIntermediateResultBlockBuilderImpl();
		final int blockSize = 30;
		return new ResultBlockIterOverResultElementIter( elmtIter, blockBuilder, blockSize );
	}


	protected static class ExecutablePlanImpl implements ExecutablePlan
	{
		private final ExecutableOperator rootOp;

		public ExecutablePlanImpl( final ExecutableOperator rootOp ) {
			assert rootOp != null;
			this.rootOp = rootOp;
		}

		@Override
		public ExecutableOperator getRootOperator() { return rootOp; }

	} // end of class ExecutablePlanImpl


	protected static class ExecutableRootOperator implements ExecutableOperator
	{
		private final ResultElementIterator it;

		public ExecutableRootOperator( final ResultElementIterator it ) {
			assert it != null;
			this.it = it;
		}

		public ResultElementIterator getResultElementIterator() { return it; }
	}

}
