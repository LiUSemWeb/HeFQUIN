package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.cardinality;

import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.utils.CompletableFutureUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
/**
 * This class implements a cardinality estimation approach that relies on the cardinalities of subPlans.
 * Specifically, for the join operator of two subPlans, the join cardinality is determined as the minimum of the cardinalities of its subPlans.
 * In the case of a subPlan with Union as the root operator, the cardinality is estimated by summing the cardinalities of each subPlan.
 * ( as outlined on page 1052 in the paper[1]).
 *
 * [1] Heling, Lars, and Maribel Acosta. "Federated SPARQL query processing over heterogeneous linked data fragments." Proceedings of the ACM Web Conference 2022.
 */

public class MinBasedCardinalityEstimationImpl extends CardinalityEstimationImpl {

    public MinBasedCardinalityEstimationImpl(final FederationAccessManager fedAccessMgr ) {
        super( fedAccessMgr );
    }

    public MinBasedCardinalityEstimationImpl(final QueryProcContext ctxt ) {
        super( ctxt );
    }

    @Override
    public CompletableFuture<Integer> _initiateCardinalityEstimation( final PhysicalPlan plan ) {
        final LogicalOperator rootOp = ((PhysicalOperatorForLogicalOperator) plan.getRootOperator()).getLogicalOperator();

        Supplier<Integer> worker;
        if ( rootOp instanceof LogicalOpRequest ) {
            worker = new WorkerForRequestOps( (LogicalOpRequest<?, ?>) rootOp);
        }
        else if (  rootOp instanceof LogicalOpJoin  ){
            // The join cardinality is the minimum cardinality of subPlans
            worker = new WorkerForJoin( plan.getSubPlan(0), plan.getSubPlan(1) );
        }
        else if ( rootOp instanceof UnaryLogicalOp ) {
            worker = new WorkerForJoin( plan.getSubPlan(0), PhysicalPlanFactory.extractRequestAsPlan((UnaryLogicalOp) rootOp));
        }
        else if ( rootOp instanceof LogicalOpMultiwayUnion || rootOp instanceof LogicalOpUnion ) {
            // The estimated cardinality is the sum up cardinality of subPlans
            worker = new WorkerForUnion( plan );
        }
        else {
            throw new IllegalArgumentException("The type of the root operator of the given plan is currently not supported (" + rootOp.getClass().getName() + ").");
        }

        return CompletableFuture.supplyAsync(worker);
    }

    protected class WorkerForJoin implements Supplier<Integer>
    {
        protected final PhysicalPlan plan1;
        protected final PhysicalPlan plan2;

        public WorkerForJoin( final PhysicalPlan plan1, final PhysicalPlan plan2 ) {
            this.plan1 = plan1;
            this.plan2 = plan2;
        }

        @Override
        public Integer get() {
            final CompletableFuture<Integer>[] futures = new CompletableFuture[2];
            futures[0] = initiateCardinalityEstimation( plan1 );
            futures[1] = initiateCardinalityEstimation( plan2 );

            Integer[] cardinalities;
            try {
                cardinalities = CompletableFutureUtils.getAll(futures, Integer.class);
            }
            catch ( final CompletableFutureUtils.GetAllException ex ) {
                if ( ex.getCause() != null && ex.getCause() instanceof InterruptedException ) {
                    throw new RuntimeException("Unexpected interruption when getting a variable-specific cardinality estimate.", ex.getCause() );
                }
                else {
                    throw new RuntimeException("Getting a variable-specific cardinality estimate caused an exception.", ex.getCause() );
                }
            }

            return Math.min( cardinalities[0], cardinalities[1] );
        }

    }

    protected class WorkerForUnion implements Supplier<Integer>
    {
        protected final PhysicalPlan plan;

        public WorkerForUnion(final PhysicalPlan plan) {
            this.plan = plan;
        }

        @Override
        public Integer get() {
            final Integer[] cardinalities = getEstimatedCardsOfSubPlans( plan );
            int cardinality = 0;
            for ( int i = 0; i < plan.numberOfSubPlans(); ++i ) {
                int intValue = cardinalities[i];
                cardinality += intValue < 0? Integer.MAX_VALUE: intValue;
                cardinality = cardinality < 0? Integer.MAX_VALUE: cardinality;
            }

            return cardinality;
        }

        protected Integer[] getEstimatedCardsOfSubPlans( final PhysicalPlan plan ) {
            final int numOfSubPlans = plan.numberOfSubPlans();
            final CompletableFuture<Integer>[] futures = new CompletableFuture[numOfSubPlans];

            for ( int i = 0; i < numOfSubPlans; i++ ) {
                final PhysicalPlan p = plan.getSubPlan(i);
                futures[i] = initiateCardinalityEstimation( p );
            }

            final Integer[] cardinalities;
            try {
                cardinalities = CompletableFutureUtils.getAll(futures, Integer.class);
            }
            catch ( final CompletableFutureUtils.GetAllException ex ) {
                if ( ex.getCause() != null && ex.getCause() instanceof InterruptedException ) {
                    throw new RuntimeException("Unexpected interruption when getting a variable-specific cardinality estimate.", ex.getCause() );
                }
                else {
                    throw new RuntimeException("Getting a variable-specific cardinality estimate caused an exception.", ex.getCause() );
                }
            }
            return cardinalities;
        }
    }

}
