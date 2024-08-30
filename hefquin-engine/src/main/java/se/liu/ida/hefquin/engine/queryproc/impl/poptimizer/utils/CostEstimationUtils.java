package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.base.utils.CompletableFutureUtils;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostEstimationException;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;

public class CostEstimationUtils
{
	/**
	 * Initiates the cost estimation processes for all the given plans and, 
	 * afterwards, waits for the resulting estimates and returns them.
	 * The returned array contains as many values as plans are given to this
	 * function, where the i-th value is for the i-th plan that is given.
	 */
	public static Double[] getEstimates( final CostModel costModel,
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
	public static Double[] getEstimates( final CostModel costModel,
	                                     final List<PhysicalPlan> plans )
			throws CostEstimationException
	{
//		TODO: Temporarily set the batch size as 50
		final List<List<PhysicalPlan>> blockOfPlans = PhysicalPlanWithCostUtils.slicePlans(plans, 50);

		final List<Double> costsOfPlans= new ArrayList<>();
		for ( List<PhysicalPlan> oneBlockOfPlans : blockOfPlans ) {
			@SuppressWarnings("unchecked")
			final CompletableFuture<Double>[] futures = new CompletableFuture[oneBlockOfPlans.size()];
			for (int i = 0; i < oneBlockOfPlans.size(); ++i) {
				futures[i] = costModel.initiateCostEstimation(oneBlockOfPlans.get(i));
			}

			try {
				Double[] costs= CompletableFutureUtils.getAll(futures, Double.class);
				costsOfPlans.addAll(List.of(costs));
			}
			catch ( final CompletableFutureUtils.GetAllException ex ) {
				if ( ex.getCause() != null && ex.getCause() instanceof InterruptedException ) {
					throw new CostEstimationException("Unexpected interruption when getting a cost estimate.", ex.getCause(), plans.get(ex.i) );
				}
				else {
					throw new CostEstimationException("Getting a cost estimate caused an exception.", ex.getCause(), plans.get(ex.i) );
				}
			}
		}
		return costsOfPlans.toArray(new Double[plans.size()]);
	}

}
