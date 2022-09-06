package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;

public interface HeuristicForLogicalOptimization
{
	/**
	 * Applies this heuristics to the given plan and returns
	 * the resulting, potentially rewritten plan.
	 */
	LogicalPlan apply( LogicalPlan inputPlan );

}
