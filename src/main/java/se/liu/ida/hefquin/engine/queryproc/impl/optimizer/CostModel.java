package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface CostModel
{
	/**
	 * Starts an internal process that, first, estimates the cost of the given
	 * plan and, afterwards, calls the given processor with the result of this
	 * estimation.
	 *
	 * Implementations of this method may be asynchronous. That is, they may
	 * return immediately after starting the cost estimation process. Hence,
	 * code that uses this method has to check (and perhaps wait) that the
	 * given processor has been called and has completed its task.
	 */
	void initiateCostEstimation( PhysicalPlan p, CostEstimateProcessor ceProc ) throws CostEstimationException;
}
