package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;

/**
 * An abstract base class for implementations of {@link JoinPlanOptimizer}.
 */
public abstract class JoinPlanOptimizerBase implements JoinPlanOptimizer
{
	@Override
	public final PhysicalPlan determineJoinPlan( final List<PhysicalPlan> subplans,
	                                             final QueryProcContext ctxt )
			throws PhysicalOptimizationException
	{
		// no need to use the enumeration algorithm if there is only one subplan
		if ( subplans.size() == 1 ) {
			return subplans.get(0);
		}

		final EnumerationAlgorithm algo = initializeEnumerationAlgorithm(subplans, ctxt);
		return algo.getResultingPlan();
	}

	protected abstract EnumerationAlgorithm initializeEnumerationAlgorithm(
			List<PhysicalPlan> subplans,
			QueryProcContext ctxt );

	protected interface EnumerationAlgorithm
	{
		PhysicalPlan getResultingPlan() throws PhysicalOptimizationException;
	}

}
