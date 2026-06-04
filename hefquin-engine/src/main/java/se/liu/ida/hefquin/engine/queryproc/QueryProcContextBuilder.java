package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.federation.access.FederationAccessManager;

public class QueryProcContextBuilder
{
	protected final FederationAccessManager fedAccMgr;
//	protected final FederationCatalog fedCatalog;
//	protected final ExecutorService execServiceForPlanTasks;
//	protected final LogicalToPhysicalPlanConverter lp2pp;
//	protected final LogicalToPhysicalOpConverter lop2pop;

	protected boolean isExperimentRun = false;
	protected boolean skipExecution = false;

	public QueryProcContextBuilder( final FederationAccessManager fedAccMgr //,
//	                                final FederationCatalog fedCatalog,
//	                                final ExecutorService execServiceForPlanTasks,
//	                                final LogicalToPhysicalPlanConverter lp2pp,
//	                                final LogicalToPhysicalOpConverter lop2pop 
) {
		assert fedAccMgr != null;
//		assert fedCatalog != null;
//		assert execServiceForPlanTasks != null;
//		assert lp2pp != null;
//		assert lop2pop != null;

		this.fedAccMgr = fedAccMgr;
//		this.fedCatalog = fedCatalog;
//		this.execServiceForPlanTasks = execServiceForPlanTasks;
//		this.lp2pp = lp2pp;
//		this.lop2pop = lop2pop;
	}

	public QueryProcContextBuilder setIsExperimentRun() {
		isExperimentRun = true;
		return this;
	}

	public QueryProcContextBuilder setSkipExecution() {
		skipExecution = true;
		return this;
	}

	public QueryProcContext2 build() {
		return new QueryProcContext2() {
			@Override
			public FederationAccessManager getFederationAccessMgr() {
				return fedAccMgr;
			}

			@Override
			public boolean skipExecution() {
				return skipExecution;
			}

			@Override
			public boolean isExperimentRun() {
				return isExperimentRun;
			}
		};
	}
}
