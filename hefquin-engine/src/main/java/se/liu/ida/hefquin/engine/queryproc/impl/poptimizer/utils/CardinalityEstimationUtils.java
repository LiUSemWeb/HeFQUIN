package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.base.utils.CompletableFutureUtils;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CardinalityEstimation;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CardinalityEstimationException;

public class CardinalityEstimationUtils
{
	/**
	 * Initiates the cardinality estimation processes for all the given plans 
	 * and, afterwards, waits for the resulting estimates and returns them.
	 * The returned array contains as many values as plans are given to this
	 * function, where the i-th value is for the i-th plan that is given.
	 *
	 * Note that calls of this function are synchronous; that is, they return
	 * only after the cardinality estimation processes have completed and the
	 * estimates have been determined. For asynchronous functions, use the
	 * given {@link CardinalityEstimation} directly.
	 */
	public static Integer[] getEstimates( final CardinalityEstimation cardEstimate,
	                                      final PhysicalPlan... plans )
			throws CardinalityEstimationException
	{
		return getEstimates( cardEstimate, Arrays.asList(plans) );
	}

	/**
	 * Initiates the cardinality estimation processes for all the plans in
	 * the given list and, afterwards, waits for the resulting estimates and
	 * returns them. The returned array contains as many values as there are
	 * plans in the given list, where the i-th value in the array is for the
	 * i-th plan in the given list.
	 *
	 * Note that calls of this function are synchronous; that is, they return
	 * only after the cardinality estimation processes have completed and the
	 * estimates have been determined. For asynchronous functions, use the
	 * given {@link CardinalityEstimation} directly.
	 */
	public static Integer[] getEstimates( final CardinalityEstimation cardEstimate,
	                                      final List<PhysicalPlan> plans )
			throws CardinalityEstimationException
	{
//		TODO: Temporarily set the batch size as 50
		final List<List<PhysicalPlan>> blockOfPlans = PhysicalPlanWithCostUtils.slicePlans(plans, 50);

		final List<Integer> costsOfPlans= new ArrayList<>();
		for ( List<PhysicalPlan> oneBlockOfPlans: blockOfPlans ){
			@SuppressWarnings("unchecked")
			final CompletableFuture<Integer>[] futures = new CompletableFuture[oneBlockOfPlans.size()];
			for (int i = 0; i < oneBlockOfPlans.size(); ++i) {
				futures[i] = cardEstimate.initiateCardinalityEstimation(oneBlockOfPlans.get(i));
			}

			try {
				Integer[] cardinalities= CompletableFutureUtils.getAll(futures, Integer.class);
				costsOfPlans.addAll(List.of(cardinalities));
			} catch (final CompletableFutureUtils.GetAllException ex) {
				if (ex.getCause() != null && ex.getCause() instanceof InterruptedException) {
					throw new CardinalityEstimationException("Unexpected interruption when getting a cardinality estimate.", ex.getCause(), plans.get(ex.i));
				} else {
					throw new CardinalityEstimationException("Getting a cardinality estimate caused an exception.", ex.getCause(), plans.get(ex.i));
				}
			}
		}
		return costsOfPlans.toArray(new Integer[plans.size()]);
	}

}
