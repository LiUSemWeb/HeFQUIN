package se.liu.ida.hefquin.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
}
