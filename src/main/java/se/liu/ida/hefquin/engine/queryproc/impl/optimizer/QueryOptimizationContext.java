package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

public interface QueryOptimizationContext extends QueryProcContext
{
	LogicalToPhysicalPlanConverter getLogicalToPhysicalPlanConverter();
}
