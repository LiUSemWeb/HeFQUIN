package se.liu.ida.hefquin.engine.queryproc.impl.compiler;

import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased.IteratorBasedExecutablePlanImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased.ResultElementIterWithBinaryExecOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased.ResultElementIterWithNullaryExecOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased.ResultElementIterWithUnaryExecOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased.ResultElementIterator;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.QueryCompilationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

public class QueryPlanCompilerForIteratorBasedExecution extends QueryPlanCompilerBase
{
	public QueryPlanCompilerForIteratorBasedExecution( final QueryProcContext ctxt ) {
		super(ctxt);
	}

	@Override
	public ExecutablePlan compile( final PhysicalPlan qep )
			throws QueryCompilationException
	{
		final ExecutionContext execCxt = createExecContext();
		final ResultElementIterator it = compile( qep, execCxt );
		return new IteratorBasedExecutablePlanImpl(it);
	}

	protected ResultElementIterator compile( final PhysicalPlan qep,
	                                         final ExecutionContext execCxt )
	{
		final QueryPlanningInfo qpInfo;
		if ( qep.hasQueryPlanningInfo() )
			qpInfo = qep.getQueryPlanningInfo();
		else
			qpInfo = null;

		if ( qep.numberOfSubPlans() == 0 )
		{
			final NullaryExecutableOp execOp = (NullaryExecutableOp) qep.getRootOperator().createExecOp(true, qpInfo);
			return new ResultElementIterWithNullaryExecOp(execOp, execCxt);
		}
		else if ( qep.numberOfSubPlans() == 1 )
		{
			final PhysicalPlan subPlan = qep.getSubPlan(0);

			final UnaryExecutableOp execOp = (UnaryExecutableOp) qep.getRootOperator().createExecOp( true, qpInfo, subPlan.getExpectedVariables() );

			final ResultElementIterator elmtIterSubPlan = compile(subPlan, execCxt);
			return new ResultElementIterWithUnaryExecOp(execOp, elmtIterSubPlan, execCxt);
		}
		else if ( qep.numberOfSubPlans() == 2 )
		{
			final PhysicalPlan subPlan1 = qep.getSubPlan(0);
			final PhysicalPlan subPlan2 = qep.getSubPlan(1);

			final BinaryExecutableOp execOp = (BinaryExecutableOp) qep.getRootOperator().createExecOp(
					true,
					qpInfo,
					subPlan1.getExpectedVariables(),
					subPlan2.getExpectedVariables() );

			final ResultElementIterator elmtIterSubPlan1 = compile(subPlan1, execCxt);
			final ResultElementIterator elmtIterSubPlan2 = compile(subPlan2, execCxt);
			return new ResultElementIterWithBinaryExecOp(execOp, elmtIterSubPlan1, elmtIterSubPlan2, execCxt);
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}

}
