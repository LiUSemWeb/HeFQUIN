package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.utils.Pair;

public interface QueryOptimizer
{
	Pair<PhysicalPlan, QueryOptimizationStats> optimize( final LogicalPlan initialPlan ) throws QueryOptimizationException;
}
