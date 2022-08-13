package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryproc.LogicalQueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.LogicalQueryOptimizer;

public class LogicalQueryOptimizerImpl implements LogicalQueryOptimizer
{
	@Override
	public LogicalPlan optimize( final LogicalPlan inputPlan, final boolean keepNaryOperators ) throws LogicalQueryOptimizationException {
		return inputPlan;
	}

}
