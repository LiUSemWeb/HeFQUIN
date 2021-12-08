package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;
import org.apache.jena.sparql.util.Context;

import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizerFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.EvolutionaryAlgorithmQueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.TerminatedByNumberOfGenerations;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.TerminationCriterion;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.DPBasedJoinPlanOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.GreedyJoinPlanOptimizerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.JoinPlanOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.SimpleJoinOrderingQueryOptimizer;
import se.liu.ida.hefquin.jenaintegration.sparql.HeFQUINConstants;

public class ModEngineConfig extends ModBase
{
	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
        // nothing to do at the moment
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		// nothing to do at the moment
	}

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
		final TerminationCriterion termination = new TerminatedByNumberOfGenerations(20);
		return new EvolutionaryAlgorithmQueryOptimizer(ctxt, 8, 2, termination);
	}

}
