package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.EvolutionaryAlgorithmQueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.TerminatedByNumberOfGenerations;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.TerminationCriterion;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.DPBasedJoinPlanOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.GreedyJoinPlanOptimizerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.JoinPlanOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.SimpleJoinOrderingQueryOptimizer;

public class QueryOptimizerImpl implements QueryOptimizer
{
	protected final QueryOptimizationContext ctxt;

	public QueryOptimizerImpl( final QueryOptimizationContext ctxt ) {
		assert ctxt != null;
		this.ctxt = ctxt;
	}

	@Override
	public PhysicalPlan optimize( final LogicalPlan initialPlan )
			throws QueryOptimizationException
	{
		// TODO query optimization: currently, different optimization algorithms can be applied by uncommenting the corresponding part of the code below.
		// TODO adds an argument to the command line that allows us to choose which algorithm to use

		final boolean keepMultiwayJoins = false;
		final PhysicalPlan initialPhysicalPlan = ctxt.getLogicalToPhysicalPlanConverter().convert(initialPlan, keepMultiwayJoins);
		return initialPhysicalPlan;

		// Uncomment the following part to apply an evolutionary algorithm for query optimization:
/*
		final TerminationCriterion termination = new TerminatedByNumberOfGenerations(20);
		final QueryOptimizer queryOptimize = new EvolutionaryAlgorithmQueryOptimizer(ctxt, 8, 2, termination);
		return queryOptimize.optimize( initialPlan );
*/

		// Uncomment the following part to apply a dynamic programming algorithm or greedy algorithm for query optimization:
/*
		// Greedy algorithm:
		final JoinPlanOptimizer joinOpti = new GreedyJoinPlanOptimizerImpl( ctxt.getCostModel() );
		// Dynamic programming algorithm:
		// final JoinPlanOptimizer joinOpti = new DPBasedJoinPlanOptimizer( ctxt );

		final QueryOptimizer queryOptimize = new SimpleJoinOrderingQueryOptimizer( joinOpti, ctxt );
		return queryOptimize.optimize( initialPlan );
 */

	}

}
