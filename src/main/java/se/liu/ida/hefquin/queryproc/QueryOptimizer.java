package se.liu.ida.hefquin.queryproc;

import se.liu.ida.hefquin.queryplan.LogicalPlan;
import se.liu.ida.hefquin.queryplan.PhysicalPlan;

public interface QueryOptimizer
{
	PhysicalPlan optimize( final LogicalPlan initialPlan );
}
