package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostEstimationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostModel;

public class CostEstimationUtils
{
	/**
	 * Initiates the cost estimation processes for all the given plans and, 
	 * afterwards, waits for the resulting estimates and returns them.
	 * The returned array contains as many values as plans are given to this
	 * function, where the i-th value is for the i-th plan that is given.
	 */
	public static double[] getEstimates( final CostModel costModel,
	                                     final PhysicalPlan... plans )
			throws CostEstimationException
	{
		return getEstimates( costModel, Arrays.asList(plans) );
	}

	/**
	 * Initiates the cost estimation processes for all the plans in the
	 * given list and, afterwards, waits for the resulting estimates and
	 * returns them.
	 * The returned array contains as many values as there are plans in
	 * the given list, where the i-th value in the array is for the i-th
	 * plan in the given list.
	 */
	public static double[] getEstimates( final CostModel costModel,
	                                     final List<PhysicalPlan> plans )
			throws CostEstimationException
	{
		final CompletableFuture<?>[] futures = new CompletableFuture[plans.size()];
		for ( int i = 0; i < plans.size(); ++i ) {
			futures[i] = costModel.initiateCostEstimation( plans.get(i) );
		}

		final double[] costs = new double[plans.size()];
		CostEstimationException ex = null;
		for ( int i = 0; i < plans.size(); ++i ) {
			if ( ex == null ) {
				try {
					costs[i] = (Double) futures[i].get();
				}
				catch ( final InterruptedException e ) {
					ex = new CostEstimationException("Unexpected interruption when getting a cost estimate.", e, plans.get(i));
				}
				catch ( final ExecutionException e ) {
					ex = new CostEstimationException("Getting a cost estimate caused an exception.", e, plans.get(i));
				}
			}
			else {
				futures[i].cancel(true);
			}
		}

		if ( ex != null ) {
			throw ex;
		}

		return costs;
	}

}
