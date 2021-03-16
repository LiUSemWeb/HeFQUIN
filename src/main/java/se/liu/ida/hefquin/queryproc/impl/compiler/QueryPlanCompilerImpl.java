package se.liu.ida.hefquin.queryproc.impl.compiler;

import se.liu.ida.hefquin.query.SolutionMapping;
import se.liu.ida.hefquin.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.queryplan.ExecutableOperatorCreator;
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
		final ResultElementIterator<SolutionMapping> it = compile( qep.getRootOperator(), execCxt );
		final ExecutableOperator<SolutionMapping> execOp = new ExecutableRootOperator<SolutionMapping>(it);
		return new ExecutablePlanImpl(execOp);
	}

	protected ResultElementIterator<SolutionMapping> compile(
			final PhysicalOperator rootOp,
			final ExecutionContext execCxt )
	{
		final ExecutableOperatorCreator<?> execOpCreator = rootOp.getExecOpCreator();
		final ExecutableOperatorCreator<SolutionMapping> execOpCreator2 = (ExecutableOperatorCreator<SolutionMapping>) execOpCreator;
		final ExecutableOperator<SolutionMapping> execOp = execOpCreator2.createOp(rootOp);

		if ( rootOp.numberOfChildren() == 0 )
		{
			if ( ! (execOp  instanceof NullaryExecutableOp<?>) )
				throw new IllegalArgumentException();

			return new ResultElementIterWithNullaryExecOp<SolutionMapping>(
					(NullaryExecutableOp<SolutionMapping>) execOp,
					execCxt );
		}
		else if ( rootOp.numberOfChildren() == 1 )
		{
			if ( ! (execOp  instanceof UnaryExecutableOp<?,?>) )
				throw new IllegalArgumentException();

			final ResultElementIterator<SolutionMapping> elmtIterChild = compile(rootOp.getChild(0), execCxt);
			final ResultBlockIterator<SolutionMapping> blockIterChild = createBlockIterator(elmtIterChild);

			return new ResultElementIterWithUnaryExecOp<SolutionMapping,SolutionMapping>(
					(UnaryExecutableOp<SolutionMapping,SolutionMapping>) execOp,
					blockIterChild,
					execCxt );
		}
		else if ( rootOp.numberOfChildren() == 2 )
		{
			if ( ! (execOp  instanceof BinaryExecutableOp<?,?,?>) )
				throw new IllegalArgumentException();

			final ResultElementIterator<SolutionMapping> elmtIterChild1 = compile(rootOp.getChild(0), execCxt);
			final ResultBlockIterator<SolutionMapping> blockIterChild1 = createBlockIterator(elmtIterChild1);

			final ResultElementIterator<SolutionMapping> elmtIterChild2 = compile(rootOp.getChild(0), execCxt);
			final ResultBlockIterator<SolutionMapping> blockIterChild2 = createBlockIterator(elmtIterChild2);

			return new ResultElementIterWithBinaryExecOp<SolutionMapping,SolutionMapping,SolutionMapping>(
					(BinaryExecutableOp<SolutionMapping,SolutionMapping,SolutionMapping>) execOp,
					blockIterChild1,
					blockIterChild2,
					execCxt );
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

	protected ResultBlockIterator<SolutionMapping> createBlockIterator( final ResultElementIterator<SolutionMapping> elmtIter ) {
		final IntermediateResultBlockBuilder<SolutionMapping> blockBuilder = new GenericIntermediateResultBlockBuilderImpl<SolutionMapping>();
		final int blockSize = 30;
		return new ResultBlockIterOverResultElementIter<SolutionMapping>( elmtIter, blockBuilder, blockSize  );
	}


	protected static class ExecutablePlanImpl implements ExecutablePlan
	{
		private final ExecutableOperator<?> rootOp;

		public ExecutablePlanImpl( final ExecutableOperator<?> rootOp ) {
			assert rootOp != null;
			this.rootOp = rootOp;
		}

		@Override
		public ExecutableOperator<?> getRootOperator() { return rootOp; }

	} // end of class ExecutablePlanImpl


	protected static class ExecutableRootOperator<ElmtType> implements ExecutableOperator<ElmtType>
	{
		private final ResultElementIterator<ElmtType> it;

		public ExecutableRootOperator( final ResultElementIterator<ElmtType> it ) {
			assert it != null;
			this.it = it;
		}

		public ResultElementIterator<ElmtType> getResultElementIterator() { return it; }
	}

}
