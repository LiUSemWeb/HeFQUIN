package se.liu.ida.hefquin.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.impl.AsyncFederationAccessManagerImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.FederationAccessManagerWithCache;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverterImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionEngine;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanCompiler;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.impl.compiler.QueryPlanCompilerForPushBasedExecution;
import se.liu.ida.hefquin.engine.queryproc.impl.execution.ExecutionEngineImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicsBasedLogicalOptimizerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CardinalityEstimation;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.PhysicalOptimizerWithoutOptimization;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.cardinality.CardinalityEstimationImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel.CostModelImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.srcsel.ServiceClauseBasedSourcePlannerImpl;

/**
 * A collection of functions to create the default versions
 * of the various components used by the HeFQUIN engine.
 */
public class HeFQUINEngineDefaultComponents
{
	public static int DEFAULT_THREAD_POOL_SIZE = 10;

	public static ExecutorService createExecutorServiceForPlanTasks() {
		return Executors.newCachedThreadPool();
		//return Executors.newFixedThreadPool(20);
	}

	public static ExecutorService createExecutorServiceForFedAccess() {
		//return Executors.newCachedThreadPool();
		return Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
	}

	public static FederationAccessManager createDefaultFederationAccessManager( final ExecutorService execService ) {
		final FederationAccessManager internalFedAccMgr = new AsyncFederationAccessManagerImpl(execService);
		final int cacheCapacity = 100;
		return new FederationAccessManagerWithCache(internalFedAccMgr, cacheCapacity);
	}

	public static CostModel createDefaultCostModel( final QueryProcContext ctx ) {
		final CardinalityEstimation cardEst = new CardinalityEstimationImpl(ctx);
		return new CostModelImpl(cardEst);
	}

	public static SourcePlanner createDefaultSourcePlanner( final QueryProcContext ctx ) {
		return new ServiceClauseBasedSourcePlannerImpl(ctx);
	}

	public static LogicalOptimizer createDefaultLogicalOptimizer( final QueryProcContext ctx ) {
		return new HeuristicsBasedLogicalOptimizerImpl( ctx,
		                                                HeuristicsBasedLogicalOptimizerImpl.getDefaultHeuristics(ctx) );
	}

	public static PhysicalOptimizer createDefaultPhysicalOptimizer() {
		final LogicalToPhysicalPlanConverter l2p = createDefaultLogicalToPhysicalPlanConverter();
		return new PhysicalOptimizerWithoutOptimization(l2p);
	}

	public static LogicalToPhysicalPlanConverter createDefaultLogicalToPhysicalPlanConverter() {
		final boolean ignorePhysicalOpsForLogicalAddOps = false;
		final boolean ignoreParallelMultiLeftJoin = false;
		return new LogicalToPhysicalPlanConverterImpl(ignorePhysicalOpsForLogicalAddOps,
		                                              ignoreParallelMultiLeftJoin);
	}

	public static QueryPlanCompiler createDefaultPlanCompiler( final QueryProcContext ctx ) {
		return new QueryPlanCompilerForPushBasedExecution(ctx);
	}

	public static ExecutionEngine createDefaultExecutionEngine() {
		return new ExecutionEngineImpl();
	}

}
