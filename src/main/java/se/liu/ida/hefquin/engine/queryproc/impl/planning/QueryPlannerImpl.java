package se.liu.ida.hefquin.engine.queryproc.impl.planning;

import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanner;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;

/**
 * Simple implementation of {@link QueryPlanner}.
 */
public class QueryPlannerImpl implements QueryPlanner
{
	protected final SourcePlanner sourcePlanner;
	protected final QueryOptimizer optimizer;

	public QueryPlannerImpl( final SourcePlanner sourcePlanner,
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
