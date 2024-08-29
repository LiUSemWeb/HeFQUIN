package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;

public interface LogicalOptimizer
{
	LogicalPlan optimize( LogicalPlan inputPlan, boolean keepNaryOperators ) throws LogicalOptimizationException;
}
