package se.liu.ida.hefquin.engine.datastructures.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

import java.util.*;

public class SolutionMappingsHashTableBasedOnOneVar extends SolutionMappingsIndexBase
{
    protected final Map<Node, List<SolutionMapping>> map = new HashMap<>();
    protected final Var joinVar;

    public SolutionMappingsHashTableBasedOnOneVar(final Var joinVar) {
        this.joinVar = joinVar;
    }

    @Override
    public int size() {
        int size = 0;
        for ( final List<SolutionMapping> li : map.values() ) {
        	size += li.size();
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
    	for ( final List<SolutionMapping> li : map.values() ) {
    		if ( ! li.isEmpty() ) {
    			return false;
    		}
    	}
        return true;
    }

    @Override
    public boolean contains(Object o) {
        if( !(o instanceof SolutionMapping) ){
            return false;
        }

        final Binding b = ((SolutionMapping) o).asJenaBinding();
        for ( final List<SolutionMapping> li : map.values() ) {
                for (final SolutionMapping sm : li) {
                    if (sm.asJenaBinding().equals(b)) {
                        return true;
                    }
                }
        }
        return false;
    }

    @Override
    public Iterator<SolutionMapping> iterator() {
        final List<SolutionMapping> solMap = new ArrayList<>();

        for ( final List<SolutionMapping> l : map.values() ) {
        	solMap.addAll(l);
        }
        return solMap.iterator();
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
    public void clear() {
    	for ( final List<SolutionMapping> li : map.values() ) {
    		li.clear();
    	}
    	map.clear();
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

    @Override
    public Iterable<SolutionMapping> findSolutionMappings(
    		final Var var1, final Node value1,
    		final Var var2, final Node value2,
    		final Var var3, final Node value3 )
    				throws UnsupportedOperationException {
    	throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<SolutionMapping> findSolutionMappings(
    		final Var var1, final Node value1,
    		final Var var2, final Node value2 )
    				throws UnsupportedOperationException {
    	throw new UnsupportedOperationException();
    }

    protected Node getVarKey( final SolutionMapping e ) {
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
