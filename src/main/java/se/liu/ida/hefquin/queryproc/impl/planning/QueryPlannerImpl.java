package se.liu.ida.hefquin.queryproc.impl.planning;

import se.liu.ida.hefquin.query.Query;
import se.liu.ida.hefquin.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.queryproc.QueryPlanner;
import se.liu.ida.hefquin.queryproc.SourcePlanner;

/**
 * Simple implementation of {@link QueryPlanner}.
 */
public class QueryPlannerImpl implements QueryPlanner
{
	protected final SourcePlanner sourcePlanner;
	protected final QueryOptimizer optimizer;

	QueryPlannerImpl( final SourcePlanner sourcePlanner,
			          final QueryOptimizer optimizer ) {
		assert sourcePlanner != null;
		assert optimizer != null;

		this.sourcePlanner = sourcePlanner;
		this.optimizer = optimizer;
	}

	public SourcePlanner getSourcePlanner() { return sourcePlanner; }

	public QueryOptimizer getOptimizer() { return optimizer; }

	public PhysicalPlan createPlan( final Query query ) {
		return optimizer.optimize( sourcePlanner.createSourceAssignment(query) );
	}

}
