package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimation;

import java.util.*;

public class CostModel {
    protected final QueryProcContext ctxt;
    protected final CostOfRootOperator costOfRootOperator;
    protected final RootOperatorCostCache rootOperatorCostCache = new RootOperatorCostCache();

    public CostModel(QueryProcContext ctxt ) {
        this.ctxt = ctxt;
        this.costOfRootOperator = new CostOfRootOperator( new CardinalityEstimation(ctxt) );
    }

    public Double wrapUpCostAsOneValue( final PhysicalPlan pp ) throws QueryOptimizationException {
        List<Integer> measureMetrics = totalCostOfPhysicalPlan( pp, new ArrayList<Integer>() );
        ArrayList<Double> weight= new ArrayList<Double>(Arrays.asList( 0.2, 0.2, 0.2, 0.2, 0.2 ));

        double cost = 0;
        for (  int i = 0; i < measureMetrics.size(); i++ ){
            cost = cost + measureMetrics.get(i) * weight.get(i);
        }

        return cost;
    }

    protected List<Integer> totalCostOfPhysicalPlan( final PhysicalPlan pp, final List<Integer> totalCostOfEachMetric ) throws QueryOptimizationException {

        for ( int i = 0; i < pp.numberOfSubPlans(); i++ ){
            final List<Integer> costOfRootOp = costOfRootOperator( pp.getSubPlan(i) );
            for ( int j = 0; j < costOfRootOp.size(); j++ ){
                // the total cost of a physical plan: sum up the cost of all operators based on each metric
                totalCostOfEachMetric.set( j, totalCostOfEachMetric.get(j) + costOfRootOp.get(j) ) ;
            }
            totalCostOfPhysicalPlan( pp.getSubPlan(i), totalCostOfEachMetric);
        }

        return totalCostOfEachMetric;
    }

    protected List<Integer> costOfRootOperator(final PhysicalPlan pp ) throws QueryOptimizationException {
        final List<Integer> cachedRootCost = rootOperatorCostCache.get(pp);
        if ( cachedRootCost != null ){
            return cachedRootCost;
        }

        final List<Integer> metrics = new ArrayList<Integer>();
        metrics.add( costOfRootOperator.getNumberOfRequests( pp ) );
        metrics.add( costOfRootOperator.getShippedRDFTermsForRequests( pp ) );
        metrics.add( costOfRootOperator.getShippedRDFVarsForRequests( pp ) );
        metrics.add( costOfRootOperator.getShippedRDFTermsForResponses( pp ) );
        metrics.add( costOfRootOperator.getShippedRDFVarsForResponses( pp ) );
        metrics.add( costOfRootOperator.getIntermediateResultsSize( pp ) );

        rootOperatorCostCache.add( pp, metrics );
        return metrics;
    }

}