package se.liu.ida.hefquin.engine.queryproc;

import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.catalog.FederationCatalog;

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

	/**
	 * Returns <code>true</code> if the user requested to skip the actual
	 * query execution. In this case, the query processor can stop after
	 * query planning.
	 */
	boolean skipExecution();
}
