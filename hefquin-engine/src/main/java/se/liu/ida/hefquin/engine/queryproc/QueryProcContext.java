package se.liu.ida.hefquin.engine.queryproc;

import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalOpConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;

public interface QueryProcContext
{
	FederationAccessManager getFederationAccessMgr();

	LogicalToPhysicalPlanConverter getLogicalToPhysicalPlanConverter();

	LogicalToPhysicalOpConverter getLogicalToPhysicalOpConverter();

	ExecutorService getExecutorServiceForPlanTasks();
}
