package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;

public class QueryOptimizerImpl implements QueryOptimizer
{
	protected final LogicalToPhysicalPlanConverter l2pConverter;

	public QueryOptimizerImpl( final LogicalToPhysicalPlanConverter l2pConverter ) {
		assert l2pConverter != null;
		this.l2pConverter = l2pConverter;
	}

	@Override
	public PhysicalPlan optimize( final LogicalPlan initialPlan ) {
		final PhysicalPlan initialPhysicalPlan = l2pConverter.convert(initialPlan);
		return initialPhysicalPlan;

		// TODO implement query optimization
	}

}
