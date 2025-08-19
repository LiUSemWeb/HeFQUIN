package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.cardinality;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CardinalityEstimation;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CardinalityEstimationException;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.CardinalityEstimationUtils;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;

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

    public MinBasedCardinalityEstimationImpl( final FederationAccessManager fedAccessMgr ) {
        super( fedAccessMgr );
    }

    public MinBasedCardinalityEstimationImpl( final QueryProcContext ctxt ) {
        super( ctxt );
    }

    @Override
    public CompletableFuture<Integer> _initiateCardinalityEstimation( final PhysicalPlan plan ) {
        final LogicalOperator rootOp = ((PhysicalOperatorForLogicalOperator) plan.getRootOperator()).getLogicalOperator();

        Supplier<Integer> worker;
        if ( rootOp instanceof LogicalOpRequest ) {
            worker = new WorkerForRequestOps(plan);
        }
        else if (  rootOp instanceof LogicalOpJoin  ){
            // The join cardinality is the minimum cardinality of subPlans
            worker = new WorkerForJoin( this, plan.getSubPlan(0), plan.getSubPlan(1) );
        }
        else if ( rootOp instanceof LogicalOpGPAdd gpAdd ) {
            worker = new WorkerForJoin( this, plan.getSubPlan(0), PhysicalPlanFactory.extractRequestAsPlan(gpAdd));
        }
        else if ( rootOp instanceof LogicalOpMultiwayUnion || rootOp instanceof LogicalOpUnion ) {
            // The estimated cardinality is the sum up cardinality of subPlans
            worker = new WorkerForUnion( this, plan );
        }
        else if ( rootOp instanceof LogicalOpLocalToGlobal || rootOp instanceof LogicalOpGlobalToLocal ) {
            return _initiateCardinalityEstimation( plan.getSubPlan(0) );
        }
        else {
            throw new IllegalArgumentException("The type of the root operator of the given plan is currently not supported (" + rootOp.getClass().getName() + ").");
        }

        return CompletableFuture.supplyAsync(worker);
    }

    protected class WorkerForJoin implements Supplier<Integer>
    {
        protected final CardinalityEstimation cardEstimate;
        protected final PhysicalPlan plan1;
        protected final PhysicalPlan plan2;

        public WorkerForJoin( final CardinalityEstimation cardEstimate, final PhysicalPlan plan1, final PhysicalPlan plan2 ) {
            this.cardEstimate = cardEstimate;
            this.plan1 = plan1;
            this.plan2 = plan2;
        }

        @Override
        public Integer get() {
            Integer[] cardinalities = new Integer[0];
            try {
                cardinalities = CardinalityEstimationUtils.getEstimates( cardEstimate, plan1, plan2);
            } catch ( CardinalityEstimationException e ) {
                e.printStackTrace();
            }

            return Math.min( cardinalities[0], cardinalities[1] );
        }

    }

    protected class WorkerForUnion implements Supplier<Integer>
    {
        protected final CardinalityEstimation cardEstimate;
        protected final PhysicalPlan plan;

        public WorkerForUnion( final CardinalityEstimation cardEstimate, final PhysicalPlan plan) {
            this.cardEstimate = cardEstimate;
            this.plan = plan;
        }

        @Override
        public Integer get() {
            final int numOfSubPlans = plan.numberOfSubPlans();
            final PhysicalPlan[] subPlans = new PhysicalPlan[numOfSubPlans];
            for ( int i = 0; i < numOfSubPlans; i++ ) {
                subPlans[i] = plan.getSubPlan(i);
            }

            final Integer[] cardinalities;
            try {
                cardinalities = CardinalityEstimationUtils.getEstimates( cardEstimate, subPlans );
            }
            catch ( final CardinalityEstimationException e ) {
                throw new RuntimeException("Getting cardinality estimates caused an exception.", e);
            }

            int cardinality = 0;
            for ( int i = 0; i < plan.numberOfSubPlans(); ++i ) {
                int intValue = cardinalities[i];
                cardinality += intValue < 0? Integer.MAX_VALUE: intValue;
                cardinality = cardinality < 0? Integer.MAX_VALUE: cardinality;
            }

            return cardinality;
        }

    }

}
