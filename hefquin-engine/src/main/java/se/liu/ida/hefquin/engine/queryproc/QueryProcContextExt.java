package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalOpConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;

public interface QueryProcContextExt extends QueryProcContext2
{
	LogicalToPhysicalPlanConverter getLogicalToPhysicalPlanConverter();

	LogicalToPhysicalOpConverter getLogicalToPhysicalOpConverter();
}
