package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

public interface LogicalToPhysicalPlanConverter
{
	PhysicalPlan convert( LogicalPlan lp, boolean keepMultiwayJoins );
}
