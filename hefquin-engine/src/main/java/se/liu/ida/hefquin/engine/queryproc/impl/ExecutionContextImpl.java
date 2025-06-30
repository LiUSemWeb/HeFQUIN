package se.liu.ida.hefquin.engine.queryproc.impl;

import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.catalog.FederationCatalog;

public class ExecutionContextImpl implements ExecutionContext
{
	protected final FederationAccessManager fedAccMgr;
	protected final FederationCatalog fedCatalog;
	protected final ExecutorService execServiceForPlanTasks;
	protected final boolean isExperimentRun;
	protected final boolean skipExecution;

	public ExecutionContextImpl( final FederationAccessManager fedAccMgr,
	                             final FederationCatalog fedCatalog,
	                             final ExecutorService execServiceForPlanTasks,
	                             final boolean isExperimentRun,
	                             final boolean skipExecution ) {
		assert fedAccMgr != null;
		assert fedCatalog != null;
		assert execServiceForPlanTasks != null;

		this.fedAccMgr = fedAccMgr;
		this.fedCatalog = fedCatalog;
		this.execServiceForPlanTasks = execServiceForPlanTasks;
		this.isExperimentRun = isExperimentRun;
		this.skipExecution = skipExecution;
	}

	@Override
	public FederationAccessManager getFederationAccessMgr() { return fedAccMgr; }

	@Override
	public FederationCatalog getFederationCatalog() { return fedCatalog; }

	@Override
	public ExecutorService getExecutorServiceForPlanTasks() { return execServiceForPlanTasks; }

	@Override
	public boolean isExperimentRun() { return isExperimentRun; }

	@Override
	public boolean skipExecution() { return skipExecution; }

}
