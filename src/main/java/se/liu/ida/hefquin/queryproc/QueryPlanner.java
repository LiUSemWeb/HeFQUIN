package se.liu.ida.hefquin.queryproc;

import se.liu.ida.hefquin.query.Query;
import se.liu.ida.hefquin.queryplan.PhysicalPlan;

public interface QueryPlanner
{
	PhysicalPlan createPlan( final Query query );

	SourcePlanner getSourcePlanner();

	QueryOptimizer getOptimizer();
}
