package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;

/**
 * An abstract base class for implementations of {@link JoinPlanOptimizer}.
 */
public abstract class JoinPlanOptimizerBase implements JoinPlanOptimizer
{
	@Override
	public final PhysicalPlan determineJoinPlan( final List<PhysicalPlan> subplans,
	                                             final QueryProcContextExt ctx )
			throws PhysicalOptimizationException
	{
		// no need to use the enumeration algorithm if there is only one subplan
		if ( subplans.size() == 1 ) {
			return subplans.get(0);
		}

		final EnumerationAlgorithm algo = initializeEnumerationAlgorithm(subplans, ctx);
		return algo.getResultingPlan();
	}

	protected abstract EnumerationAlgorithm initializeEnumerationAlgorithm(
			List<PhysicalPlan> subplans,
			QueryProcContextExt ctx );

	protected interface EnumerationAlgorithm
	{
		PhysicalPlan getResultingPlan() throws PhysicalOptimizationException;
	}

}
