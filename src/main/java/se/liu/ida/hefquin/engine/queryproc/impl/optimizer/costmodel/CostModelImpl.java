package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostEstimateProcessor;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimationImpl;

import java.util.*;

public class CostModelImpl implements CostModel
{
    protected final QueryProcContext ctxt;
    protected final PhysicalPlanCostCache physicalPlanCostCache;

    public CostModelImpl( final QueryProcContext ctxt ) {
        assert ctxt != null;
        this.ctxt = ctxt;
        this.physicalPlanCostCache = new PhysicalPlanCostCache();
    }

    public void initiateCostEstimation( final PhysicalPlan pp,
                              final CostEstimateProcessor ceProc )
            throws QueryOptimizationException
    {
        final CostFunctionsForPhysicalPlans costFunctionsForPP = new CostFunctionsForPhysicalPlansImpl( new CardinalityEstimationImpl(ctxt) );

        CostOfPhysicalPlan costOfPhysicalPlan = physicalPlanCostCache.get(pp);
        if ( costOfPhysicalPlan == null ){
            costOfPhysicalPlan = costFunctionsForPP.determineCostOfPhysicalPlan( pp );
            physicalPlanCostCache.add( pp,  costOfPhysicalPlan);
        }

        ArrayList<Double> weight= new ArrayList<Double>(Arrays.asList( 0.2, 0.2, 0.2, 0.2, 0.2 ));

        final double cost =  costOfPhysicalPlan.getNumberOfRequests() * weight.get(0) + costOfPhysicalPlan.getShippedRDFTermsForRequests() * weight.get(1)
                + costOfPhysicalPlan.getShippedVarsForRequests() * weight.get(2) + costOfPhysicalPlan.getShippedRDFTermsForResponses() * weight.get(3)
                + costOfPhysicalPlan.getShippedVarsForResponses() * weight.get(4) + costOfPhysicalPlan.getIntermediateResultsSize() * weight.get(5);

        ceProc.process(cost, pp);
    }

}
