package se.liu.ida.hefquin.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.sparql.util.Context;

import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverterImpl;
import se.liu.ida.hefquin.engine.queryproc.PhysicalQueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.PhysicalQueryOptimizerFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.PhysicalQueryOptimizerImpl;
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
	public static int DEFAULT_THREAD_POOL_SIZE = 10;
	public final boolean LogicalToPhysicalPlanConverter_ignorePhysicalOpsForLogicalAddOps;
	public final boolean LogicalToPhysicalPlanConverter_ignoreParallelMultiLeftJoin;

	public HeFQUINEngineConfig( final boolean LogicalToPhysicalPlanConverter_ignorePhysicalOpsForLogicalAddOps,
	                            final boolean LogicalToPhysicalPlanConverter_ignoreParallelMultiLeftJoin ) {
		this.LogicalToPhysicalPlanConverter_ignorePhysicalOpsForLogicalAddOps = LogicalToPhysicalPlanConverter_ignorePhysicalOpsForLogicalAddOps;
		this.LogicalToPhysicalPlanConverter_ignoreParallelMultiLeftJoin = LogicalToPhysicalPlanConverter_ignoreParallelMultiLeftJoin;
	}

	public void initializeContext( final Context ctxt ) {
		ctxt.set( HeFQUINConstants.sysQueryOptimizerFactory, createQueryOptimizerFactory() );
	}

	public LogicalToPhysicalPlanConverter createLogicalToPhysicalPlanConverter() {
		return new LogicalToPhysicalPlanConverterImpl( LogicalToPhysicalPlanConverter_ignorePhysicalOpsForLogicalAddOps,
		                                               LogicalToPhysicalPlanConverter_ignoreParallelMultiLeftJoin );
	}

	public ExecutorService createExecutorServiceForPlanTasks() {
		return Executors.newCachedThreadPool();
		//return Executors.newFixedThreadPool(20);
	}

	public ExecutorService createExecutorServiceForFedAccess() {
		//return Executors.newCachedThreadPool();
		return Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
	}

	protected PhysicalQueryOptimizerFactory createQueryOptimizerFactory() {
		// TODO: Instead of hard-coding the following, the optimizer (and
		// similar things) should be created based on a config file.
		return new PhysicalQueryOptimizerFactory() {
			@Override
			public PhysicalQueryOptimizer createQueryOptimizer( final QueryOptimizationContext ctxt ) {
				return createQueryOptimizerWithoutOptimization(ctxt);
//				return createGreedyJoinPlanOptimizer(ctxt);
//				return createDPBasedJoinPlanOptimizer(ctxt);
//				return createEvolutionaryAlgorithmQueryOptimizer(ctxt);
			}
		};
	}

	protected PhysicalQueryOptimizer createQueryOptimizerWithoutOptimization( final QueryOptimizationContext ctxt ) {
		return new PhysicalQueryOptimizerImpl(ctxt);
	}

	protected PhysicalQueryOptimizer createGreedyJoinPlanOptimizer( final QueryOptimizationContext ctxt ) {
		final JoinPlanOptimizer joinOpt = new GreedyJoinPlanOptimizerImpl( ctxt.getCostModel() );
		return new SimpleJoinOrderingQueryOptimizer(joinOpt, ctxt);
	}

	protected PhysicalQueryOptimizer createDPBasedJoinPlanOptimizer( final QueryOptimizationContext ctxt ) {
		final JoinPlanOptimizer joinOpt = new DPBasedJoinPlanOptimizer(ctxt);
		return new SimpleJoinOrderingQueryOptimizer(joinOpt, ctxt);
	}

	protected PhysicalQueryOptimizer createEvolutionaryAlgorithmQueryOptimizer( final QueryOptimizationContext ctxt ) {
		final TerminationCriterionFactory tcFactory = TerminatedByNumberOfGenerations.getFactory(20);
		return new EvolutionaryAlgorithmQueryOptimizer(ctxt, 8, 2, tcFactory);
	}

}
