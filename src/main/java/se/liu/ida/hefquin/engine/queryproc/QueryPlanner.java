package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface QueryPlanner
{
	PhysicalPlan createPlan( final Query query ) throws QueryPlanningException;

	SourcePlanner getSourcePlanner();

	QueryOptimizer getOptimizer();
}
