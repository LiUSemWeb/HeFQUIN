package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

public interface RewritingRuleForPhysicalOptimization
{
	/**
	 * Applies this heuristics to the given plan and returns
	 * the resulting, potentially rewritten plan.
	 */
	PhysicalPlan apply( PhysicalPlan inputPlan, QueryProcContext ctxt ) throws PhysicalOptimizationException;

}
