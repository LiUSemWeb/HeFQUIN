package se.liu.ida.hefquin.engine.queryproc.impl;

import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalOpConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;

public class ExecutionContextImpl implements ExecutionContext
{
	protected final FederationAccessManager fedAccMgr;
	protected final ExecutorService execServiceForPlanTasks;
	protected final LogicalToPhysicalPlanConverter lp2pp;
	protected  final LogicalToPhysicalOpConverter lop2pop;

	public ExecutionContextImpl( final FederationAccessManager fedAccMgr,
	                             final ExecutorService execServiceForPlanTasks,
	                             final LogicalToPhysicalPlanConverter lp2pp,
	                             final LogicalToPhysicalOpConverter lop2pop ) {
		assert fedAccMgr != null;
		assert execServiceForPlanTasks != null;
		assert lp2pp != null;
		assert lop2pop != null;

		this.fedAccMgr = fedAccMgr;
		this.execServiceForPlanTasks = execServiceForPlanTasks;
		this.lp2pp = lp2pp;
		this.lop2pop = lop2pop;
	}

	@Override
	public FederationAccessManager getFederationAccessMgr() { return fedAccMgr; }

	@Override
	public ExecutorService getExecutorServiceForPlanTasks() { return execServiceForPlanTasks; }

	@Override
	public LogicalToPhysicalPlanConverter getLogicalToPhysicalPlanConverter() { return lp2pp; }

	@Override
	public LogicalToPhysicalOpConverter getLogicalToPhysicalOpConverter() { return lop2pop; }

}
