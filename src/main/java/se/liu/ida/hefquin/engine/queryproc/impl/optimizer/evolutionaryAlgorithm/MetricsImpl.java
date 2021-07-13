package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import java.util.List;

public class MetricsImpl implements Metrics{
    protected List<Integer> li;

    public MetricsImpl( List<Integer> li) {
        this.li = li;
    }

    @Override
    public Integer getNumberOfRequests() {
        return li.get(0);
    }

    @Override
    public Integer getShippedRDFTermsForRequests() {
        return li.get(1);
    }

    @Override
    public Integer getShippedRDFVarsForRequests() {
        return li.get(2);
    }

    @Override
    public Integer getShippedRDFTermsForResponses() {
        return li.get(3);
    }

    @Override
    public Integer getShippedRDFVarsForResponses() {
        return li.get(4);
    }

    @Override
    public Integer getIntermediateResultsSize() {
        return li.get(5);
    }

    @Override
    public Boolean isEmpty() {
        return li.isEmpty();
    }

}
