package se.liu.ida.hefquin.engine;

import org.apache.jena.sparql.util.Context;

import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizerFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.EvolutionaryAlgorithmQueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.TerminatedByNumberOfGenerations;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.TerminationCriterionFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.DPBasedJoinPlanOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.GreedyJoinPlanOptimizerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.JoinPlanOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.SimpleJoinOrderingQueryOptimizer;
import se.liu.ida.hefquin.jenaintegration.sparql.HeFQUINConstants;

public class HeFQUINEngineConfig
{
	public void initializeContext( final Context ctxt ) {
		ctxt.set( HeFQUINConstants.sysQueryOptimizerFactory, createQueryOptimizerFactory() );
	}

	protected QueryOptimizerFactory createQueryOptimizerFactory() {
		// TODO: Instead of hard-coding the following, the optimizer (and
		// similar things) should be created based on a config file.
		return new QueryOptimizerFactory() {
			@Override
			public QueryOptimizer createQueryOptimizer( final QueryOptimizationContext ctxt ) {
				return createQueryOptimizerWithoutOptimization(ctxt);
//				return createGreedyJoinPlanOptimizer(ctxt);
//				return createDPBasedJoinPlanOptimizer(ctxt);
//				return createEvolutionaryAlgorithmQueryOptimizer(ctxt);
			}
		};
	}

	protected QueryOptimizer createQueryOptimizerWithoutOptimization( final QueryOptimizationContext ctxt ) {
		return new QueryOptimizerImpl(ctxt);
	}

	protected QueryOptimizer createGreedyJoinPlanOptimizer( final QueryOptimizationContext ctxt ) {
		final JoinPlanOptimizer joinOpt = new GreedyJoinPlanOptimizerImpl( ctxt.getCostModel() );
		return new SimpleJoinOrderingQueryOptimizer(joinOpt, ctxt);
	}

	protected QueryOptimizer createDPBasedJoinPlanOptimizer( final QueryOptimizationContext ctxt ) {
		final JoinPlanOptimizer joinOpt = new DPBasedJoinPlanOptimizer(ctxt);
		return new SimpleJoinOrderingQueryOptimizer(joinOpt, ctxt);
	}

	protected QueryOptimizer createEvolutionaryAlgorithmQueryOptimizer( final QueryOptimizationContext ctxt ) {
		final TerminationCriterionFactory tcFactory = TerminatedByNumberOfGenerations.getFactory(20);
		return new EvolutionaryAlgorithmQueryOptimizer(ctxt, 8, 2, tcFactory);
	}

}
