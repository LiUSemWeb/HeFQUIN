package se.liu.ida.hefquin.engine.queryproc;

import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.catalog.FederationCatalog;

public class QueryProcContextBuilder
{
	protected final FederationCatalog fedCatalog;
	protected final FederationAccessManager fedAccMgr;
	protected final ExecutorService execServiceForPlanTasks;

	protected boolean isExperimentRun = false;
	protected boolean skipExecution = false;

	public QueryProcContextBuilder( final FederationCatalog fedCatalog,
	                                final FederationAccessManager fedAccMgr,
	                                final ExecutorService execServiceForPlanTasks ) {
		assert fedCatalog != null;
		assert fedAccMgr != null;
		assert execServiceForPlanTasks != null;

		this.fedCatalog = fedCatalog;
		this.fedAccMgr = fedAccMgr;
		this.execServiceForPlanTasks = execServiceForPlanTasks;
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
			public FederationCatalog getFederationCatalog() {
				return fedCatalog;
			}

			@Override
			public FederationAccessManager getFederationAccessMgr() {
				return fedAccMgr;
			}

			@Override
			public ExecutorService getExecutorServiceForPlanTasks() {
				return execServiceForPlanTasks;
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
