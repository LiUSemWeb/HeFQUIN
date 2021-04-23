package se.liu.ida.hefquin.queryproc.impl.optimizer;

import se.liu.ida.hefquin.queryplan.LogicalPlan;
import se.liu.ida.hefquin.queryplan.PhysicalPlan;

public interface LogicalToPhysicalPlanConverter
{
	PhysicalPlan convert( LogicalPlan lp );
}
