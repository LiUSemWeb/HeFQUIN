package se.liu.ida.hefquin.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverterImpl;

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


}
