package se.liu.ida.hefquin.engine.datastructures.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

import java.util.*;

public class SolutionMappingsHashTableBasedOnOneVar extends SolutionMappingsHashTable{
    protected final Map<Node, List<SolutionMapping>> map = new HashMap<>();
    protected final Var joinVar;

    public SolutionMappingsHashTableBasedOnOneVar(final Var joinVar) {
        this.joinVar = joinVar;
    }

    @Override
    public boolean add( final SolutionMapping e ) {
        final Node valKeys = getVarKey(e);

        if (valKeys != null ){
            List<SolutionMapping> solMapList = map.get(valKeys);
            if ( solMapList == null) {
                solMapList = new ArrayList<>();
                map.put(valKeys, solMapList);
            }
            solMapList.add(e);
            return true;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Iterable<SolutionMapping> getJoinPartners( final SolutionMapping sm )
            throws UnsupportedOperationException
    {
        final Node valKeys = getVarKey(sm);
        final Iterator<SolutionMapping> matchingSolMaps;
        if ( valKeys == null ){
            matchingSolMaps = iterator();
        }
        else {
            final List<SolutionMapping> l = map.get(valKeys);
            matchingSolMaps = (l != null) ? l.iterator() : null;
        }

        final List<SolutionMapping> joinPartner = new ArrayList<>();
        while (matchingSolMaps != null && matchingSolMaps.hasNext()) {
            final SolutionMapping matchSolM = matchingSolMaps.next();
            if (SolutionMappingUtils.compatible(sm, matchSolM)) {
                joinPartner.add(matchSolM);
            }
        }
        return joinPartner;
    }

    @Override
    public Iterable<SolutionMapping> findSolutionMappings( final Var var, final Node value )
            throws UnsupportedOperationException
    {
        if ( joinVar.equals(var) ){
            List<SolutionMapping> solMapList = map.get(value);
            if ( solMapList == null) {
                solMapList = new ArrayList<>();
            }
            return solMapList;
        }
        else{
            throw new UnsupportedOperationException();
        }
    }

    protected Node getVarKey(final SolutionMapping e){
        final Binding solMapBinding = e.asJenaBinding();
        final Node n = solMapBinding.get(joinVar);
        if ( n == null){
            return null;
        }
        else {
            return n;
        }
    }
}
