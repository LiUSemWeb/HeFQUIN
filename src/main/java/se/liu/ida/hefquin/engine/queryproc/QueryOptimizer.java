package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface QueryOptimizer
{
	PhysicalPlan optimize( final LogicalPlan initialPlan ) throws QueryOptimizationException;
}
