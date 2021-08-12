package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public class CardinalityEstimationUtils
{
	/**
	 * Initiates the cardinality estimation processes for all the given plans 
	 * and, afterwards, waits for the resulting estimates and returns them.
	 * The returned array contains as many values as plans are given to this
	 * function, where the i-th value is for the i-th plan that is given.
	 */
	public static int[] getEstimates( final CardinalityEstimation cardEstimate,
	                                  final PhysicalPlan... plans )
			throws CardinalityEstimationException
	{
		return getEstimates( cardEstimate, Arrays.asList(plans) );
	}

	/**
	 * Initiates the cardinality estimation processes for all the plans in
	 * the given list and, afterwards, waits for the resulting estimates and
	 * returns them.
	 * The returned array contains as many values as there are plans in the
	 * given list, where the i-th value in the array is for the i-th plan in
	 * the given list.
	 */
	public static int[] getEstimates( final CardinalityEstimation cardEstimate,
	                                  final List<PhysicalPlan> plans )
			throws CardinalityEstimationException
	{
		final CompletableFuture<?>[] futures = new CompletableFuture[plans.size()];
		for ( int i = 0; i < plans.size(); ++i ) {
			futures[i] = cardEstimate.initiateCardinalityEstimation( plans.get(i) );
		}

		final int[] costs = new int[plans.size()];
		CardinalityEstimationException ex = null;
		for ( int i = 0; i < plans.size(); ++i ) {
			if ( ex == null ) {
				try {
					costs[i] = (Integer) futures[i].get();
				}
				catch ( final InterruptedException e ) {
					ex = new CardinalityEstimationException("Unexpected interruption when getting a cardinality estimate.", e, plans.get(i));
				}
				catch ( final ExecutionException e ) {
					ex = new CardinalityEstimationException("Getting a cardinality estimate caused an exception.", e, plans.get(i));
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
