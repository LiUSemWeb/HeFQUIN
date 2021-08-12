package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostEstimationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CardinalityEstimationImpl;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CostModelImpl implements CostModel
{
	protected static final List<Double> weights = Arrays.asList( 0.2, 0.2, 0.2, 0.2, 0.2 );

    protected final QueryProcContext ctxt;
    protected final PhysicalPlanCostCache physicalPlanCostCache;

    public CostModelImpl( final QueryProcContext ctxt ) {
        assert ctxt != null;
        this.ctxt = ctxt;
        this.physicalPlanCostCache = new PhysicalPlanCostCache();
    }

    public CompletableFuture<Double> initiateCostEstimation( final PhysicalPlan pp )
            throws CostEstimationException
    {
        final CostFunctionsForPhysicalPlans costFunctionsForPP = new CostFunctionsForPhysicalPlansImpl( new CardinalityEstimationImpl(ctxt) );

        CostOfPhysicalPlan costOfPhysicalPlan = physicalPlanCostCache.get(pp);
        if ( costOfPhysicalPlan == null ){
            costOfPhysicalPlan = costFunctionsForPP.determineCostOfPhysicalPlan( pp );
            physicalPlanCostCache.add( pp,  costOfPhysicalPlan);
        }

        final double cost =
                weights.get(0) * costOfPhysicalPlan.getNumberOfRequests()
              + weights.get(1) * costOfPhysicalPlan.getShippedRDFTermsForRequests()
              + weights.get(2) * costOfPhysicalPlan.getShippedVarsForRequests()
              + weights.get(3) * costOfPhysicalPlan.getShippedRDFTermsForResponses()
              + weights.get(4) * costOfPhysicalPlan.getShippedVarsForResponses()
              + weights.get(5) * costOfPhysicalPlan.getIntermediateResultsSize();

        return CompletableFuture.completedFuture(cost);
    }

}
