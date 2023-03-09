package se.liu.ida.hefquin.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.sparql.util.Context;

import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverterImpl;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizerFactory;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.SourcePlannerFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.PhysicalOptimizerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.evolutionaryAlgorithm.EvolutionaryAlgorithmQueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.evolutionaryAlgorithm.TerminatedByNumberOfGenerations;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.evolutionaryAlgorithm.TerminationCriterionFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple.*;
import se.liu.ida.hefquin.engine.queryproc.impl.srcsel.ServiceClauseBasedSourcePlannerImpl;
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
		ctxt.set( HeFQUINConstants.sysSourcePlannerFactory, createSourcePlannerFactory() );
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

	protected SourcePlannerFactory createSourcePlannerFactory() {
		// TODO: Instead of hard-coding the following, the source planner (and
		// similar things) should be created based on a config file.
		return new SourcePlannerFactory() {
			@Override
			public SourcePlanner createSourcePlanner( final QueryProcContext ctxt ) {
				return new ServiceClauseBasedSourcePlannerImpl(ctxt);
//				return new ExhaustiveSourcePlannerImpl(ctxt);
			}
		};
	}

	protected PhysicalOptimizerFactory createQueryOptimizerFactory() {
		// TODO: Instead of hard-coding the following, the optimizer (and
		// similar things) should be created based on a config file.
		return new PhysicalOptimizerFactory() {
			@Override
			public PhysicalOptimizer createQueryOptimizer( final QueryOptimizationContext ctxt ) {
//				return createQueryOptimizerWithoutOptimization(ctxt);
//				return createGreedyJoinPlanOptimizer(ctxt);
//				return createGreedyBasedTwoPhaseJoinPlanOptimizerImpl(ctxt);
				return createDPBasedBushyJoinPlanOptimizer(ctxt);
//				return createDPBasedLinearJoinPlanOptimizer(ctxt);
//				return createEvolutionaryAlgorithmQueryOptimizer(ctxt);
			}
		};
	}

	protected PhysicalOptimizer createQueryOptimizerWithoutOptimization( final QueryOptimizationContext ctxt ) {
		return new PhysicalOptimizerImpl(ctxt);
	}

	protected PhysicalOptimizer createGreedyJoinPlanOptimizer( final QueryOptimizationContext ctxt ) {
		final JoinPlanOptimizer joinOpt = new CostModelBasedGreedyJoinPlanOptimizerImpl( ctxt.getCostModel() );
		return new SimpleJoinOrderingQueryOptimizer(joinOpt, ctxt);
	}

	protected PhysicalOptimizer createGreedyBasedTwoPhaseJoinPlanOptimizerImpl( final QueryOptimizationContext ctxt ) {
		final JoinPlanOptimizer joinOpt = new CardinalityBasedGreedyJoinPlanOptimizerImpl( ctxt.getFederationAccessMgr() );
		return new SimpleJoinOrderingQueryOptimizer(joinOpt, ctxt);
	}

	protected PhysicalOptimizer createDPBasedBushyJoinPlanOptimizer( final QueryOptimizationContext ctxt ) {
		final JoinPlanOptimizer joinOpt = new DPBasedBushyJoinPlanOptimizer(ctxt);
		return new SimpleJoinOrderingQueryOptimizer(joinOpt, ctxt);
	}

	protected PhysicalOptimizer createDPBasedLinearJoinPlanOptimizer( final QueryOptimizationContext ctxt ) {
		final JoinPlanOptimizer joinOpt = new DPBasedLinearJoinPlanOptimizer(ctxt);
		return new SimpleJoinOrderingQueryOptimizer(joinOpt, ctxt);
	}

	protected PhysicalOptimizer createEvolutionaryAlgorithmQueryOptimizer( final QueryOptimizationContext ctxt ) {
		final TerminationCriterionFactory tcFactory = TerminatedByNumberOfGenerations.getFactory(20);
		return new EvolutionaryAlgorithmQueryOptimizer(ctxt, 8, 2, tcFactory);
	}

}
