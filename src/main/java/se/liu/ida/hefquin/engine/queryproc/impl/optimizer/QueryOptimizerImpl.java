package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;

public class QueryOptimizerImpl implements QueryOptimizer
{
	protected final QueryOptimizationContext ctxt;

	public QueryOptimizerImpl( final QueryOptimizationContext ctxt ) {
		assert ctxt != null;
		this.ctxt = ctxt;
	}

	@Override
	public PhysicalPlan optimize( final LogicalPlan initialPlan )
			throws QueryOptimizationException
	{
		final PhysicalPlan initialPhysicalPlan = ctxt.getLogicalToPhysicalPlanConverter().convert(initialPlan);
		return initialPhysicalPlan;

		// TODO implement query optimization
	}

}
