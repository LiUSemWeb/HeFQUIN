package se.liu.ida.hefquin.engine.queryproc.impl;

import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalOpConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.catalog.FederationCatalog;

public class ExecutionContextImpl implements ExecutionContext
{
	protected final FederationAccessManager fedAccMgr;
	protected final FederationCatalog fedCatalog;
	protected final ExecutorService execServiceForPlanTasks;
	protected final LogicalToPhysicalPlanConverter lp2pp;
	protected  final LogicalToPhysicalOpConverter lop2pop;
	protected final boolean isExperimentRun;
	protected final boolean skipExecution;

	public ExecutionContextImpl( final FederationAccessManager fedAccMgr,
	                             final FederationCatalog fedCatalog,
	                             final ExecutorService execServiceForPlanTasks,
	                             final LogicalToPhysicalPlanConverter lp2pp,
	                             final LogicalToPhysicalOpConverter lop2pop,
	                             final boolean isExperimentRun,
	                             final boolean skipExecution ) {
		assert fedAccMgr != null;
		assert fedCatalog != null;
		assert execServiceForPlanTasks != null;
		assert lp2pp != null;
		assert lop2pop != null;

		this.fedAccMgr = fedAccMgr;
		this.fedCatalog = fedCatalog;
		this.execServiceForPlanTasks = execServiceForPlanTasks;
		this.lp2pp = lp2pp;
		this.lop2pop = lop2pop;
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
	public LogicalToPhysicalPlanConverter getLogicalToPhysicalPlanConverter() { return lp2pp; }

	@Override
	public LogicalToPhysicalOpConverter getLogicalToPhysicalOpConverter() { return lop2pop; }

	@Override
	public boolean isExperimentRun() { return isExperimentRun; }

	@Override
	public boolean skipExecution() { return skipExecution; }

}
