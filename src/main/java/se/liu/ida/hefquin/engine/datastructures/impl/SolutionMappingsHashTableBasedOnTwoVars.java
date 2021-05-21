package se.liu.ida.hefquin.engine.datastructures.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

import java.util.*;

public class SolutionMappingsHashTableBasedOnTwoVars extends SolutionMappingsIndexBase{
    protected final Map<Node, Map<Node, List<SolutionMapping>>> map = new HashMap<>();
    protected final Var joinVar1;
    protected final Var joinVar2;

    public SolutionMappingsHashTableBasedOnTwoVars( final Var joinVar1, final Var joinVar2 ) {
        this.joinVar1 = joinVar1;
        this.joinVar2 = joinVar2;
    }

    @Override
    public int size() {
        int size = 0;
        for ( final Map<Node, List<SolutionMapping>> mapIn : map.values() ) {
            for ( final List<SolutionMapping> li : mapIn.values() ){
                size = size + li.size();
            }
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        for ( final Map<Node, List<SolutionMapping>> mapIn : map.values() ) {
            for ( final List<SolutionMapping> li : mapIn.values()){
                if ( ! li.isEmpty() ) {
                    return false;
                }
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
        for ( final Map<Node, List<SolutionMapping>> mapIn : map.values() ) {
            for (final List<SolutionMapping> li : mapIn.values()) {
                for (final SolutionMapping sm : li) {
                    if (sm.asJenaBinding().equals(b)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Iterator<SolutionMapping> iterator() {
        final List<SolutionMapping> solMap = new ArrayList<>();

        final Iterator<Map<Node, List<SolutionMapping>>> mapIn = map.values().iterator();
        while( mapIn.hasNext() ){
            for ( final List<SolutionMapping> l : mapIn.next().values()){
                solMap.addAll(l);
            }
        }
        return solMap.iterator();
    }

    @Override
    public boolean add( final SolutionMapping e ) {
        final Binding solMapBinding = e.asJenaBinding();
        final Node n1 = solMapBinding.get(joinVar1);
        final Node n2 = solMapBinding.get(joinVar2);

        if (n1 == null || n2 == null){
            throw new IllegalArgumentException();
        }

        final Map<Node, List<SolutionMapping>> mapIn = map.get(n1);
        List<SolutionMapping> solMapList;
        if( mapIn == null){
            final Map<Node, List<SolutionMapping>> mapL = new HashMap<>();
            solMapList = new ArrayList<>();
            mapL.put(n2, solMapList);
            map.put(n1, mapL);
        }
        else {
            solMapList = mapIn.get(n2);
            if ( solMapList == null ){
                solMapList = new ArrayList<>();
                mapIn.put(n2, solMapList);
            }
        }
        solMapList.add(e);
        return true;
    }

    @Override
    public void clear() {
        for ( final Map<Node, List<SolutionMapping>> mapIn : map.values() ) {
            for ( final List<SolutionMapping> li : mapIn.values() ) {
                li.clear();
            }
        }
        map.clear();
    }

    @Override
    public Iterable<SolutionMapping> getJoinPartners( final SolutionMapping sm )
            throws UnsupportedOperationException
    {
        final Binding solMapBinding = sm.asJenaBinding();
        final Node n1 = solMapBinding.get(joinVar1);
        final Node n2 = solMapBinding.get(joinVar2);
        Iterator<SolutionMapping> matchingSolMaps;

        if ( n1 == null ){
            matchingSolMaps = iterator();
        }
        else if (n2 == null){
            final Map<Node, List<SolutionMapping>> mapIn = map.get(n1);
            matchingSolMaps = iteratorInnerMap(mapIn);
        }
        else {
            final Iterable<SolutionMapping> l = findSolMappingList(n1, n2);
            matchingSolMaps = ( l != null) ? l.iterator() : null;
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
    public Iterable<SolutionMapping> findSolutionMappings(Var var, Node value)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<SolutionMapping> findSolutionMappings(
            final Var var1, final Node value1,
            final Var var2, final Node value2 )
            throws UnsupportedOperationException
    {
        if (var1.equals(joinVar1) && var2.equals(joinVar2)){
            Iterable<SolutionMapping> solMapList = findSolMappingList(value1, value2);
            if(solMapList == null){
                solMapList = new ArrayList<>();
            }
            return solMapList;
        }
        else if (var1.equals(joinVar2) && var2.equals(joinVar1)){
            Iterable<SolutionMapping> solMapList = findSolMappingList(value2, value1);
            if(solMapList == null){
                solMapList = new ArrayList<>();
            }
            return solMapList;
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Iterable<SolutionMapping> findSolutionMappings(Var var1, Node value1, Var var2, Node value2, Var var3, Node value3)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    protected Iterable<SolutionMapping> findSolMappingList(
            final Node keyValue1, final Node keyValue2 )
            throws UnsupportedOperationException
    {
        final Map<Node, List<SolutionMapping>> mapIn = map.get(keyValue1);
        if (mapIn == null) {
            return null;
        }
        final List<SolutionMapping> solMapList = mapIn.get(keyValue2);
        if(solMapList == null){
            return null;
        }
        return solMapList;
    }

    protected Iterator<SolutionMapping> iteratorInnerMap(final Map<Node, List<SolutionMapping>> mapIn) {
        final List<SolutionMapping> solMap = new ArrayList<>();
        for ( final List<SolutionMapping> l : mapIn.values()){
            solMap.addAll(l);
        }
        return solMap.iterator();
    }
}