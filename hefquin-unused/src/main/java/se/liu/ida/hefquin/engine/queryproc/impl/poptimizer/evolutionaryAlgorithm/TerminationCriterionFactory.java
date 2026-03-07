package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;

public interface TerminationCriterionFactory
{
	/**
	 * Creates a new instance of the corresponding termination criterion,
	 * which is initialized based on the given logical plan, and returns
	 * that new instance.
	 */
	TerminationCriterion createInstance( LogicalPlan plan );
}
