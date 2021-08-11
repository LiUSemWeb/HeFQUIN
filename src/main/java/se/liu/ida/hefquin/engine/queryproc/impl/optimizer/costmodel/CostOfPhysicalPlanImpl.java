package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

public class CostOfPhysicalPlanImpl implements CostOfPhysicalPlan {
    protected final int numberOfRequests;
    protected final int shippedRDFTermsForRequests;
    protected final int shippedVarsForRequests;
    protected final int shippedRDFTermsForResponses;
    protected final int shippedVarsForResponses;
    protected final int getIntermediateResultsSize;

    public CostOfPhysicalPlanImpl( final int numberOfRequests, final int shippedRDFTermsForRequests, final int shippedVarsForRequests, final int shippedRDFTermsForResponses, final int shippedVarsForResponses, final int getIntermediateResultsSize) {
        this.numberOfRequests = numberOfRequests;
        this.shippedRDFTermsForRequests = shippedRDFTermsForRequests;
        this.shippedVarsForRequests = shippedVarsForRequests;
        this.shippedRDFTermsForResponses = shippedRDFTermsForResponses;
        this.shippedVarsForResponses = shippedVarsForResponses;
        this.getIntermediateResultsSize = getIntermediateResultsSize;
    }

    @Override
    public int getNumberOfRequests() {
        return numberOfRequests;
    }

    @Override
    public int getShippedRDFTermsForRequests() {
        return shippedRDFTermsForRequests;
    }

    @Override
    public int getShippedVarsForRequests() {
        return shippedVarsForRequests;
    }

    @Override
    public int getShippedRDFTermsForResponses() {
        return shippedRDFTermsForResponses;
    }

    @Override
    public int getShippedVarsForResponses() {
        return shippedVarsForResponses;
    }

    @Override
    public int getIntermediateResultsSize() {
        return getIntermediateResultsSize;
    }

}
