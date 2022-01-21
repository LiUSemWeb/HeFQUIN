package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostModel;

public interface QueryProcContext
{
	FederationAccessManager getFederationAccessMgr();

	FederationCatalog getFederationCatalog();

	CostModel getCostModel();

	/**
	 * Returns <code>true</code> if the query execution process is done as part
	 * of an experiment, in which case additional statistics need to be produced.
	 */
	boolean isExperimentRun();
}
