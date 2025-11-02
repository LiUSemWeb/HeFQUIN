package se.liu.ida.hefquin.engine.queryplan.utils;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

public interface LogicalToPhysicalPlanConverter
{
	PhysicalPlan convert( LogicalPlan lp,
	                      boolean keepMultiwayJoins,
	                      QueryProcContext ctxt );
}
