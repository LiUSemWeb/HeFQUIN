package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

public class CostOfPhysicalPlanImpl implements CostOfPhysicalPlan {
    protected final int numberOfRequests;
    protected final int shippedRDFTermsForRequests;
    protected final int shippedRDFVarsForRequests;
    protected final int shippedRDFTermsForResponses;
    protected final int shippedRDFVarsForResponses;
    protected final int getIntermediateResultsSize;

    public CostOfPhysicalPlanImpl(int numberOfRequests, int shippedRDFTermsForRequests, int shippedRDFVarsForRequests, int shippedRDFTermsForResponses, int shippedRDFVarsForResponses, int getIntermediateResultsSize) {
        this.numberOfRequests = numberOfRequests;
        this.shippedRDFTermsForRequests = shippedRDFTermsForRequests;
        this.shippedRDFVarsForRequests = shippedRDFVarsForRequests;
        this.shippedRDFTermsForResponses = shippedRDFTermsForResponses;
        this.shippedRDFVarsForResponses = shippedRDFVarsForResponses;
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
    public int getShippedRDFVarsForRequests() {
        return shippedRDFVarsForRequests;
    }

    @Override
    public int getShippedRDFTermsForResponses() {
        return shippedRDFTermsForResponses;
    }

    @Override
    public int getShippedRDFVarsForResponses() {
        return shippedRDFVarsForResponses;
    }

    @Override
    public int getIntermediateResultsSize() {
        return getIntermediateResultsSize;
    }

}
