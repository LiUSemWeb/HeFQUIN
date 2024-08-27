package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.utils.Pair;

public interface QueryPlanner
{
	Pair<PhysicalPlan, QueryPlanningStats> createPlan( final Query query ) throws QueryPlanningException;

	SourcePlanner getSourcePlanner();

	LogicalOptimizer getLogicalOptimizer();

	PhysicalOptimizer getPhysicalOptimizer();
}
