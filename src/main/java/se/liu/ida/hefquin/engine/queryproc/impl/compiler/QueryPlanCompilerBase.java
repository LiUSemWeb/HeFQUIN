package se.liu.ida.hefquin.engine.queryproc.impl.compiler;

import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanCompiler;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;

public abstract class QueryPlanCompilerBase implements QueryPlanCompiler
{
	protected final QueryProcContext ctxt;

	protected QueryPlanCompilerBase( final QueryProcContext ctxt ) {
		assert ctxt != null;
		this.ctxt = ctxt;
	}

	protected ExecutionContext createExecContext() {
		return new ExecutionContext() {
			@Override public FederationCatalog getFederationCatalog() { return ctxt.getFederationCatalog(); }
			@Override public FederationAccessManager getFederationAccessMgr() { return ctxt.getFederationAccessMgr(); }
			@Override public CostModel getCostModel() { return ctxt.getCostModel(); }
			@Override public boolean isExperimentRun() { return ctxt.isExperimentRun(); }
			@Override public ExecutorService getExecutorServiceForPlanTasks() { return ctxt.getExecutorServiceForPlanTasks(); }
		};
	}

}
