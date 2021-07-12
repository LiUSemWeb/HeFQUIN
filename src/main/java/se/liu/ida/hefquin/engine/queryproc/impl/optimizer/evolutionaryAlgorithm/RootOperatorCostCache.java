package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RootOperatorCostCache {
    protected final Map<PhysicalPlan, List<Integer>> map = new HashMap<>();

    public void addNumberOfRequests( final PhysicalPlan pp, final int numberOfRequests ) {
        List<Integer> li = map.get(pp);
        if ( li == null ){
            li = new ArrayList<>();
        }

        li.add(0, numberOfRequests);
        map.put( pp, li);
    }

    public void addShippedRDFTermsForRequests( final PhysicalPlan pp, final int shippedRDFTermsForRequests ) {
        List<Integer> li = map.get(pp);
        if ( li == null ){
            li = new ArrayList<>();
        }

        li.add(1, shippedRDFTermsForRequests);
        map.put( pp, li);
    }

    public void addShippedRDFVarsForRequests( final PhysicalPlan pp, final int shippedRDFVarsForRequests ) {
        List<Integer> li = map.get(pp);
        if ( li == null ){
            li = new ArrayList<>();
        }

        li.add(2, shippedRDFVarsForRequests);
        map.put( pp, li);
    }

    public void addShippedRDFTermsForResponses( final PhysicalPlan pp, final int shippedRDFTermsForResponses ) {
        List<Integer> li = map.get(pp);
        if ( li == null ){
            li = new ArrayList<>();
        }

        li.add(3, shippedRDFTermsForResponses);
        map.put( pp, li);
    }

    public void addShippedRDFVarsForResponses( final PhysicalPlan pp, final int shippedRDFVarsForResponses ) {
        List<Integer> li = map.get(pp);
        if ( li == null ){
            li = new ArrayList<>();
        }

        li.add(4, shippedRDFVarsForResponses);
        map.put( pp, li);
    }

    public void addIntermediateResultsSize( final PhysicalPlan pp, final int intermediateResultsSize) {
        List<Integer> li = map.get(pp);
        if ( li == null ){
            li = new ArrayList<>();
        }

        li.add(5, intermediateResultsSize);
        map.put( pp, li);
    }

    public Integer getNumberOfRequests( final PhysicalPlan pp ) {
        return map.get(pp).get(0);
    }

    public Integer getShippedRDFTermsForRequests( final PhysicalPlan pp ) {
        return map.get(pp).get(1);
    }

    public Integer getShippedRDFVarsForRequests( final PhysicalPlan pp ) {
        return map.get(pp).get(2);
    }

    public Integer getShippedRDFTermsForResponses( final PhysicalPlan pp ) {
        return map.get(pp).get(3);
    }

    public Integer getShippedRDFVarsForResponses( final PhysicalPlan pp ) {
        return map.get(pp).get(4);
    }

    public Integer getIntermediateResultsSize( final PhysicalPlan pp ) {
        return map.get(pp).get(5);
    }
}
