package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimation;

import java.util.*;

public class CostModel {
    protected final QueryProcContext ctxt;
    protected final PhysicalPlanCostCache physicalPlanCostCache;

    public CostModel(QueryProcContext ctxt ) {
        this.ctxt = ctxt;
        this.physicalPlanCostCache = new PhysicalPlanCostCache();
    }

    public Double wrapUpCostAsOneValue( final PhysicalPlan pp ) throws QueryOptimizationException {
        final CostFunctionsForPhysicalPlansImpl costOfPhysicalPlan = new CostFunctionsForPhysicalPlansImpl( new CardinalityEstimation(ctxt) );
        CostOfPhysicalPlan metrics = physicalPlanCostCache.get(pp);

        if ( metrics == null ){
            final int numberOfRequests = costOfPhysicalPlan.getTotalNumberOfRequests(pp);
            final int shippedRDFTermsForRequests = costOfPhysicalPlan.getTotalShippedRDFTermsForRequests(pp);
            final int shippedRDFVarsForRequests = costOfPhysicalPlan.getTotalShippedRDFVarsForRequests(pp);
            final int shippedRDFTermsForResponses = costOfPhysicalPlan.getTotalShippedRDFTermsForResponses(pp);
            final int shippedRDFVarsForResponses = costOfPhysicalPlan.getTotalShippedVarsForResponses(pp);
            final int getIntermediateResultsSize = costOfPhysicalPlan.getTotalIntermediateResultsSize(pp);

            metrics = new CostOfPhysicalPlanImpl( numberOfRequests, shippedRDFTermsForRequests , shippedRDFVarsForRequests, shippedRDFTermsForResponses, shippedRDFVarsForResponses, getIntermediateResultsSize);
            physicalPlanCostCache.add( pp,  metrics);
        }

        ArrayList<Double> weight= new ArrayList<Double>(Arrays.asList( 0.2, 0.2, 0.2, 0.2, 0.2 ));

        final double cost =  metrics.getNumberOfRequests() * weight.get(0) + metrics.getShippedRDFTermsForRequests() * weight.get(1)
                + metrics.getShippedRDFVarsForRequests() * weight.get(2) + metrics.getShippedRDFTermsForResponses() * weight.get(3)
                + metrics.getShippedRDFVarsForResponses() * weight.get(4) + metrics.getIntermediateResultsSize() * weight.get(5);

        return cost;
    }

}