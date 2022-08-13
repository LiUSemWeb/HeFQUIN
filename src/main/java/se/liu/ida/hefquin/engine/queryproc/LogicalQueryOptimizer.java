package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;

public interface LogicalQueryOptimizer
{
	LogicalPlan optimize( LogicalPlan inputPlan, boolean keepNaryOperators ) throws LogicalQueryOptimizationException;
}
