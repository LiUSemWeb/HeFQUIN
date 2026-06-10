package se.liu.ida.hefquin.engine.queryproc.impl.compiler;

import se.liu.ida.hefquin.engine.queryproc.QueryPlanCompiler;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;

public abstract class QueryPlanCompilerBase implements QueryPlanCompiler
{
	protected final QueryProcContextExt ctx;

	protected QueryPlanCompilerBase( final QueryProcContextExt ctx ) {
		assert ctx != null;
		this.ctx = ctx;
	}

}
