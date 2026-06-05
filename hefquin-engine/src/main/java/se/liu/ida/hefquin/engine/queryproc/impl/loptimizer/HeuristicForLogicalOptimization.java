package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext2;

public interface HeuristicForLogicalOptimization
{
	/**
	 * Applies this heuristics to the given plan and returns
	 * the resulting, potentially rewritten plan.
	 */
	LogicalPlan apply( LogicalPlan inputPlan, QueryProcContext2 ctxt ) throws LogicalOptimizationException;

}
