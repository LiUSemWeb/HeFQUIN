package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostEstimationException;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;

/**
 * Attention: the tests here do NOT actually test the cost model. Instead,
 * they test whether the code in {@link CostModelImpl} works correctly;
 * e.g., does it correctly aggregate the cost values obtained from the
 * given set of cost functions, does the caching works as expected, etc.
 * To test this functionality we do not even need actual cost functions.
 */
public class CostModelImplTest extends EngineTestBase
{
	protected static boolean PRINT_TIME = false; protected static final long SLEEP_MILLIES = 0L;
	//protected static boolean PRINT_TIME = true;  protected static final long SLEEP_MILLIES = 100L;

	@Test
	public void samePlanTwiceInParallel()
			throws InterruptedException, ExecutionException, CostEstimationException
	{
		final PhysicalPlan plan = createSimplePlan();
		final CostModel costModel = createCostModel(1, 20, SLEEP_MILLIES, 2, 30, SLEEP_MILLIES);

		final long startTime = new Date().getTime();

		final CompletableFuture<Double> f1 = costModel.initiateCostEstimation(plan);
		final CompletableFuture<Double> f2 = costModel.initiateCostEstimation(plan);

		final Double result1 = f1.get();
		final Double result2 = f2.get();

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) System.out.println( "samePlanTwiceInParallel \t milliseconds passed: " + (endTime - startTime) );

		assertEquals(80, result1.doubleValue(), 0);
		assertEquals(80, result2.doubleValue(), 0);
	}

	@Test
	public void samePlanTwiceInParallel_negativeSingleCost()
			throws InterruptedException, ExecutionException, CostEstimationException
	{
		final PhysicalPlan plan = createSimplePlan();
		final CostModel costModel = createCostModel(1, Integer.MAX_VALUE+2, SLEEP_MILLIES, 2, 30, SLEEP_MILLIES);

		final long startTime = new Date().getTime();

		final CompletableFuture<Double> f1 = costModel.initiateCostEstimation(plan);
		final CompletableFuture<Double> f2 = costModel.initiateCostEstimation(plan);

		final Double result1 = f1.get();
		final Double result2 = f2.get();

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) System.out.println( "samePlanTwiceInParallel \t milliseconds passed: " + (endTime - startTime) );

		assertEquals(Integer.MAX_VALUE + 60.0, result1.doubleValue(), 0);
		assertEquals(Integer.MAX_VALUE + 60.0, result2.doubleValue(), 0);
	}

	@Test
	public void samePlanTwiceInParallel_checkTotalCost()
			throws InterruptedException, ExecutionException, CostEstimationException
	{
		final PhysicalPlan plan = createSimplePlan();
		final CostModel costModel = createCostModel(1, Integer.MAX_VALUE-1, SLEEP_MILLIES, 1, Integer.MAX_VALUE-1, SLEEP_MILLIES);

		final long startTime = new Date().getTime();

		final CompletableFuture<Double> f1 = costModel.initiateCostEstimation(plan);
		final CompletableFuture<Double> f2 = costModel.initiateCostEstimation(plan);

		final Double result1 = f1.get();
		final Double result2 = f2.get();

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) System.out.println( "samePlanTwiceInParallel \t milliseconds passed: " + (endTime - startTime) );

		assertEquals(Integer.MAX_VALUE * 2.0-2, result1.doubleValue(), 0);
		assertEquals(Integer.MAX_VALUE * 2.0-2, result2.doubleValue(), 0);
	}

	@Test
	public void samePlanTwiceInSequence()
			throws InterruptedException, ExecutionException, CostEstimationException
	{
		final PhysicalPlan plan = createSimplePlan();
		final CostModel costModel = createCostModel(1, 20, SLEEP_MILLIES, 2, 30, SLEEP_MILLIES);

		final long startTime = new Date().getTime();

		final CompletableFuture<Double> f1 = costModel.initiateCostEstimation(plan);
		final Double result1 = f1.get();

		final CompletableFuture<Double> f2 = costModel.initiateCostEstimation(plan);
		final Double result2 = f2.get();

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) System.out.println( "samePlanTwiceInSequence \t milliseconds passed: " + (endTime - startTime) );

		assertEquals(80, result1.doubleValue(), 0);
		assertEquals(80, result2.doubleValue(), 0);
	}

	@Test
	public void fourPlansInParallel()
			throws InterruptedException, ExecutionException, CostEstimationException
	{
		final PhysicalPlan plan1 = createSimplePlan();
		final PhysicalPlan plan2 = createSimplePlan();
		final PhysicalPlan plan3 = createSimplePlan();
		final PhysicalPlan plan4 = createSimplePlan();
		final CostModel costModel = createCostModel(1, 20, SLEEP_MILLIES, 2, 30, SLEEP_MILLIES);

		final long startTime = new Date().getTime();

		final CompletableFuture<Double> f1 = costModel.initiateCostEstimation(plan1);
		final CompletableFuture<Double> f2 = costModel.initiateCostEstimation(plan2);
		final CompletableFuture<Double> f3 = costModel.initiateCostEstimation(plan3);
		final CompletableFuture<Double> f4 = costModel.initiateCostEstimation(plan4);

		final Double result1 = f1.get();
		final Double result2 = f2.get();
		final Double result3 = f3.get();
		final Double result4 = f4.get();

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) System.out.println( "fourPlansInParallel \t milliseconds passed: " + (endTime - startTime) );

		assertEquals(80, result1.doubleValue(), 0);
		assertEquals(80, result2.doubleValue(), 0);
		assertEquals(80, result3.doubleValue(), 0);
		assertEquals(80, result4.doubleValue(), 0);
	}

	@Test
	public void twoPlansInSequence()
			throws InterruptedException, ExecutionException, CostEstimationException
	{
		final PhysicalPlan plan1 = createSimplePlan();
		final PhysicalPlan plan2 = createSimplePlan();
		final CostModel costModel = createCostModel(1, 20, SLEEP_MILLIES, 2, 30, SLEEP_MILLIES);

		final long startTime = new Date().getTime();

		final CompletableFuture<Double> f1 = costModel.initiateCostEstimation(plan1);
		final Double result1 = f1.get();

		final CompletableFuture<Double> f2 = costModel.initiateCostEstimation(plan2);
		final Double result2 = f2.get();

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) System.out.println( "twoPlansInSequence \t milliseconds passed: " + (endTime - startTime) );

		assertEquals(80, result1.doubleValue(), 0);
		assertEquals(80, result2.doubleValue(), 0);
	}



	protected PhysicalPlan createSimplePlan() {
		final TriplePattern tp = new TriplePatternImpl(
				NodeFactory.createBlankNode(),
				NodeFactory.createBlankNode(),
				NodeFactory.createBlankNode() );
		final TriplePatternRequest req = new TriplePatternRequestImpl(tp);
		final FederationMember fm = new TPFServerForTest();

		final LogicalOpRequest<?,?>  reqOp = new LogicalOpRequest<>(fm, req);

		return PhysicalPlanFactory.createPlanWithRequest(reqOp);
	}

	/**
	 * Creates a {@link CostModelImpl} with two fake cost functions
	 * that always return the given cost values, respectively.
	 */
	protected CostModel createCostModel( final double weight1, final int cost1, final long sleepMillis1,
	                                     final double weight2, final int cost2, final long sleepMillis2 ) {
		final CostDimension[] dims = new CostDimension[] {
				new CostDimension( weight1, new FakeCostFunctionForPlan(cost1, sleepMillis1) ),
				new CostDimension( weight2, new FakeCostFunctionForPlan(cost2, sleepMillis2) )
		};

		return new CostModelImpl(dims);
	}

	protected static class FakeCostFunctionForPlan implements CostFunctionForPlan
	{
		protected final Integer cost;
		protected final long sleepMillis;

		public FakeCostFunctionForPlan( final Integer cost, final long sleepMillis ) {
			this.cost = cost;
			this.sleepMillis = sleepMillis;
		}

		@Override
		public CompletableFuture<Integer> initiateCostEstimation( final Set<PhysicalPlan> visitedPlan, final PhysicalPlan plan ) {
			return CompletableFuture.supplyAsync( () -> {
				if ( sleepMillis > 0L ) {
					try {
						Thread.sleep(sleepMillis);
					} catch ( final InterruptedException e ) {
						throw new RuntimeException(e);
					}
				}

				return cost;
			});
		}
	}

}
