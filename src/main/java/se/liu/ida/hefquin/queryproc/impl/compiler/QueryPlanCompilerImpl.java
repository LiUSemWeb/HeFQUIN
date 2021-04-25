package se.liu.ida.hefquin.queryproc.impl.compiler;

import se.liu.ida.hefquin.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.queryplan.ExecutablePlan;
import se.liu.ida.hefquin.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlockBuilder;
import se.liu.ida.hefquin.queryplan.executable.impl.GenericIntermediateResultBlockBuilderImpl;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.BinaryExecutableOp;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.NullaryExecutableOp;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.UnaryExecutableOp;
import se.liu.ida.hefquin.queryplan.executable.impl.pullbased.PullBasedPlan;
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
		final ResultElementIterator it = compile( qep, execCxt );
		return new PullBasedPlan(it);
	}

	protected ResultElementIterator compile( final PhysicalPlan qep,
	                                         final ExecutionContext execCxt )
	{
		final ExecutableOperator execOp = qep.getRootOperator().createExecOp();

		if ( qep.numberOfSubPlans() == 0 )
		{
			if ( ! (execOp  instanceof NullaryExecutableOp) )
				throw new IllegalArgumentException();

			final NullaryExecutableOp execOp0 = (NullaryExecutableOp) execOp;
			return new ResultElementIterWithNullaryExecOp(execOp0, execCxt);
		}
		else if ( qep.numberOfSubPlans() == 1 )
		{
			if ( ! (execOp  instanceof UnaryExecutableOp) )
				throw new IllegalArgumentException();

			final ResultElementIterator elmtIterSubPlan = compile(qep.getSubPlan(0), execCxt);
			final ResultBlockIterator blockIterSubPlan = createBlockIterator( elmtIterSubPlan, execOp.preferredInputBlockSize() );

			final UnaryExecutableOp execOp1 = (UnaryExecutableOp) execOp;
			return new ResultElementIterWithUnaryExecOp(execOp1, blockIterSubPlan, execCxt);
		}
		else if ( qep.numberOfSubPlans() == 2 )
		{
			if ( ! (execOp  instanceof BinaryExecutableOp) )
				throw new IllegalArgumentException();

			final ResultElementIterator elmtIterSubPlan1 = compile(qep.getSubPlan(0), execCxt);
			final ResultBlockIterator blockIterSubPlan1 = createBlockIterator( elmtIterSubPlan1, execOp.preferredInputBlockSize() );

			final ResultElementIterator elmtIterSubPlan2 = compile(qep.getSubPlan(1), execCxt);
			final ResultBlockIterator blockIterSubPlan2 = createBlockIterator( elmtIterSubPlan2, execOp.preferredInputBlockSize() );

			final BinaryExecutableOp execOp2 = (BinaryExecutableOp) execOp;
			return new ResultElementIterWithBinaryExecOp(execOp2, blockIterSubPlan1, blockIterSubPlan2, execCxt);
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

	protected ResultBlockIterator createBlockIterator( final ResultElementIterator elmtIter, final int preferredBlockSize ) {
		final IntermediateResultBlockBuilder blockBuilder = new GenericIntermediateResultBlockBuilderImpl();
		return new ResultBlockIterOverResultElementIter( elmtIter, blockBuilder, preferredBlockSize );
	}

}
