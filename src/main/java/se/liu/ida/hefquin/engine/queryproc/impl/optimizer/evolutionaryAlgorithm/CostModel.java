package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimation;

import java.util.*;

public class CostModel {
    protected final QueryProcContext ctxt;

    public CostModel(QueryProcContext ctxt ) {
        this.ctxt = ctxt;
    }

    public Double wrapUpCostAsOneValue( final PhysicalPlan pp ) throws QueryOptimizationException {
        final CostOfPhysicalPlan costOfPhysicalPlan = new CostOfPhysicalPlan( new CardinalityEstimation(ctxt) );

        final int totalNumberOfRequests = costOfPhysicalPlan.getTotalNumberOfRequests(pp);
        final int totalShippedRDFTermsForRequests = costOfPhysicalPlan.getTotalShippedRDFTermsForRequests(pp);
        final int totalShippedRDFVarsForRequests = costOfPhysicalPlan.getTotalShippedRDFVarsForRequests(pp);
        final int totalShippedRDFTermsForResponses = costOfPhysicalPlan.getTotalShippedRDFTermsForResponses(pp);
        final int totalShippedVarsForResponses = costOfPhysicalPlan.getTotalShippedVarsForResponses(pp);
        final int totalIntermediateResultsSize = costOfPhysicalPlan.getTotalIntermediateResultsSize(pp);

        ArrayList<Double> weight= new ArrayList<Double>(Arrays.asList( 0.2, 0.2, 0.2, 0.2, 0.2 ));
        final double cost = totalNumberOfRequests * weight.get(0) + totalShippedRDFTermsForRequests * weight.get(1)
                + totalShippedRDFVarsForRequests * weight.get(2) + totalShippedRDFTermsForResponses * weight.get(3)
                + totalShippedVarsForResponses * weight.get(4) + totalIntermediateResultsSize * weight.get(5);

        return cost;
    }

}