package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

public class CostOfPhysicalPlanImpl implements CostOfPhysicalPlan {
    protected final int numberOfRequests;
    protected final int shippedRDFTermsForRequests;
    protected final int shippedVarsForRequests;
    protected final int shippedRDFTermsForResponses;
    protected final int shippedVarsForResponses;
    protected final int getIntermediateResultsSize;

    public CostOfPhysicalPlanImpl(int numberOfRequests, int shippedRDFTermsForRequests, int shippedVarsForRequests, int shippedRDFTermsForResponses, int shippedVarsForResponses, int getIntermediateResultsSize) {
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
