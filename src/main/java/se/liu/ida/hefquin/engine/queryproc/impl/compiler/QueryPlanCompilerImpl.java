package se.liu.ida.hefquin.engine.queryproc.impl.compiler;

import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.queryplan.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlockBuilder;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockBuilderImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased.PullBasedPlan;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased.ResultBlockIterOverResultElementIter;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased.ResultBlockIterator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased.ResultElementIterWithBinaryExecOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased.ResultElementIterWithNullaryExecOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased.ResultElementIterWithUnaryExecOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased.ResultElementIterator;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.QueryCompilationException;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanCompiler;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

public class QueryPlanCompilerImpl implements QueryPlanCompiler
{
	protected final QueryProcContext ctxt;

	public QueryPlanCompilerImpl( final QueryProcContext ctxt ) {
		assert ctxt != null;
		this.ctxt = ctxt;
	}

	@Override
	public ExecutablePlan compile( final PhysicalPlan qep )
			throws QueryCompilationException
	{
		final ExecutionContext execCxt = createExecContext();
		final ResultElementIterator it = compile( qep, execCxt );
		return new PullBasedPlan(it);
	}

	protected ResultElementIterator compile( final PhysicalPlan qep,
	                                         final ExecutionContext execCxt )
	{
		if ( qep.numberOfSubPlans() == 0 )
		{
			final NullaryExecutableOp execOp = (NullaryExecutableOp) qep.getRootOperator().createExecOp();
			return new ResultElementIterWithNullaryExecOp(execOp, execCxt);
		}
		else if ( qep.numberOfSubPlans() == 1 )
		{
			final PhysicalPlan subPlan = qep.getSubPlan(0);

			final UnaryExecutableOp execOp = (UnaryExecutableOp) qep.getRootOperator().createExecOp( subPlan.getExpectedVariables() );

			final ResultElementIterator elmtIterSubPlan = compile(subPlan, execCxt);
			final ResultBlockIterator blockIterSubPlan = createBlockIterator( elmtIterSubPlan, execOp.preferredInputBlockSize() );
			return new ResultElementIterWithUnaryExecOp(execOp, blockIterSubPlan, execCxt);
		}
		else if ( qep.numberOfSubPlans() == 2 )
		{
			final PhysicalPlan subPlan1 = qep.getSubPlan(0);
			final PhysicalPlan subPlan2 = qep.getSubPlan(1);

			final BinaryExecutableOp execOp = (BinaryExecutableOp) qep.getRootOperator().createExecOp(
					subPlan1.getExpectedVariables(),
					subPlan2.getExpectedVariables() );

			final ResultElementIterator elmtIterSubPlan1 = compile(subPlan1, execCxt);
			final ResultBlockIterator blockIterSubPlan1 = createBlockIterator( elmtIterSubPlan1, execOp.preferredInputBlockSize() );

			final ResultElementIterator elmtIterSubPlan2 = compile(subPlan2, execCxt);
			final ResultBlockIterator blockIterSubPlan2 = createBlockIterator( elmtIterSubPlan2, execOp.preferredInputBlockSize() );

			return new ResultElementIterWithBinaryExecOp(execOp, blockIterSubPlan1, blockIterSubPlan2, execCxt);
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}

	protected ExecutionContext createExecContext() {
		return new ExecutionContext() {
			@Override public FederationCatalog getFederationCatalog() { return ctxt.getFederationCatalog(); }
			@Override public FederationAccessManager getFederationAccessMgr() { return ctxt.getFederationAccessMgr(); }
			@Override public boolean isExperimentRun() { return ctxt.isExperimentRun(); }
		};
	}

	protected ResultBlockIterator createBlockIterator( final ResultElementIterator elmtIter, final int preferredBlockSize ) {
		final IntermediateResultBlockBuilder blockBuilder = new GenericIntermediateResultBlockBuilderImpl();
		return new ResultBlockIterOverResultElementIter( elmtIter, blockBuilder, preferredBlockSize );
	}

}
