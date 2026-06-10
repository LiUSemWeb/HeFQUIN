package se.liu.ida.hefquin.engine.queryproc.impl.compiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import se.liu.ida.hefquin.engine.queryproc.QueryCompilationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;

public class QueryPlanCompilerForIteratorBasedExecution extends QueryPlanCompilerBase
{
	private static final Logger log = LoggerFactory.getLogger( QueryPlanCompilerForIteratorBasedExecution.class );

	public QueryPlanCompilerForIteratorBasedExecution( final QueryProcContextExt ctx ) {
		super(ctx);
	}

	@Override
	public ExecutablePlan compile( final PhysicalPlan qep )
			throws QueryCompilationException
	{
		log.debug("Compiling physical plan using iterator-based execution model.");
		final ResultElementIterator it = compile(qep, ctx);
		return new IteratorBasedExecutablePlanImpl(it);
	}

	protected ResultElementIterator compile( final PhysicalPlan qep,
	                                         final QueryProcContextExt ctx )
	{
		final QueryPlanningInfo qpInfo;
		if ( qep.hasQueryPlanningInfo() )
			qpInfo = qep.getQueryPlanningInfo();
		else
			qpInfo = null;

		if ( qep.numberOfSubPlans() == 0 )
		{
			final NullaryExecutableOp execOp = (NullaryExecutableOp) qep.getRootOperator().createExecOp(true, qpInfo);
			return new ResultElementIterWithNullaryExecOp(execOp, ctx);
		}
		else if ( qep.numberOfSubPlans() == 1 )
		{
			final PhysicalPlan subPlan = qep.getSubPlan(0);

			final UnaryExecutableOp execOp = (UnaryExecutableOp) qep.getRootOperator().createExecOp( true, qpInfo, subPlan.getExpectedVariables() );

			final ResultElementIterator elmtIterSubPlan = compile(subPlan, ctx);
			return new ResultElementIterWithUnaryExecOp(execOp, elmtIterSubPlan, ctx);
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

			final ResultElementIterator elmtIterSubPlan1 = compile(subPlan1, ctx);
			final ResultElementIterator elmtIterSubPlan2 = compile(subPlan2, ctx);
			return new ResultElementIterWithBinaryExecOp(execOp, elmtIterSubPlan1, elmtIterSubPlan2, ctx);
		}
		else
		{
			log.debug(
				"Unsupported operator arity: {} for operator {}",
				qep.numberOfSubPlans(),
				qep.getRootOperator().getClass().getSimpleName() );
			throw new IllegalArgumentException();
		}
	}

}
