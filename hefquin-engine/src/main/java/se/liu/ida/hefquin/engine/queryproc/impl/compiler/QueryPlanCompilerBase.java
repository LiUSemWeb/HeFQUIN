package se.liu.ida.hefquin.engine.queryproc.impl.compiler;

import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanCompiler;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.ExecutionContextImpl;

public abstract class QueryPlanCompilerBase implements QueryPlanCompiler
{
	protected final QueryProcContext ctxt;

	protected QueryPlanCompilerBase( final QueryProcContext ctxt ) {
		assert ctxt != null;
		this.ctxt = ctxt;
	}

	protected ExecutionContext createExecContext() {
		if ( ctxt instanceof ExecutionContext eCtxt )
			return eCtxt;
		else
			return new ExecutionContextImpl( ctxt.getFederationAccessMgr(),
			                                 ctxt.getFederationCatalog(),
			                                 ctxt.getExecutorServiceForPlanTasks(),
			                                 ctxt.isExperimentRun(),
			                                 ctxt.skipExecution() );
	}

}
