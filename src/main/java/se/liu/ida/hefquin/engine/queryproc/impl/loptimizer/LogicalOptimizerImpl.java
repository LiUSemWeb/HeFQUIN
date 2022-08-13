package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizer;

public class LogicalOptimizerImpl implements LogicalOptimizer
{
	@Override
	public LogicalPlan optimize( final LogicalPlan inputPlan, final boolean keepNaryOperators ) throws LogicalOptimizationException {
		return inputPlan;
	}

}
