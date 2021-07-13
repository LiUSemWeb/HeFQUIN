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
        final CostOfPhysicalPlanImpl costOfPhysicalPlan = new CostOfPhysicalPlanImpl( new CardinalityEstimation(ctxt) );
        Metrics metrics = physicalPlanCostCache.get(pp);

        if ( metrics.isEmpty() ){
            final List<Integer> li = new ArrayList<>();
            li.add( costOfPhysicalPlan.getTotalNumberOfRequests(pp) );
            li.add( costOfPhysicalPlan.getTotalShippedRDFTermsForRequests(pp) );
            li.add( costOfPhysicalPlan.getTotalShippedRDFVarsForRequests(pp) );
            li.add( costOfPhysicalPlan.getTotalShippedRDFTermsForResponses(pp) );
            li.add( costOfPhysicalPlan.getTotalShippedVarsForResponses(pp) );
            li.add( costOfPhysicalPlan.getTotalIntermediateResultsSize(pp) );

            metrics = new MetricsImpl(li);
            physicalPlanCostCache.add( pp,  metrics);
        }

        ArrayList<Double> weight= new ArrayList<Double>(Arrays.asList( 0.2, 0.2, 0.2, 0.2, 0.2 ));

        final double cost =  metrics.getNumberOfRequests() * weight.get(0) + metrics.getShippedRDFTermsForRequests() * weight.get(1)
                + metrics.getShippedRDFVarsForRequests() * weight.get(2) + metrics.getShippedRDFTermsForResponses() * weight.get(3)
                + metrics.getShippedRDFVarsForResponses() * weight.get(4) + metrics.getIntermediateResultsSize() * weight.get(5);

        return cost;
    }

}