package se.liu.ida.hefquin.engine.queryproc;

import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;

public interface QueryProcContext
{
	FederationAccessManager getFederationAccessMgr();

	FederationCatalog getFederationCatalog();

	ExecutorService getExecutorServiceForPlanTasks();

	/**
	 * Returns <code>true</code> if the query execution process is done as part
	 * of an experiment, in which case additional statistics need to be produced.
	 */
	boolean isExperimentRun();
}
