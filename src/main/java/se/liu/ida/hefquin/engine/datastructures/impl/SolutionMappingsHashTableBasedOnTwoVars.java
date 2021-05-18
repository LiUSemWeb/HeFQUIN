package se.liu.ida.hefquin.engine.datastructures.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

import java.util.*;

public class SolutionMappingsHashTableBasedOnTwoVars extends SolutionMappingsHashTable{
    protected final Map<Node, List<Node>> mapNN = new HashMap<>();
    protected final Map<Node, List<SolutionMapping>> mapNS = new HashMap<>();
    protected final Var joinVar1;
    protected final Var joinVar2;

    public SolutionMappingsHashTableBasedOnTwoVars( final Var joinVar1, final Var joinVar2 ) {
        this.joinVar1 = joinVar1;
        this.joinVar2 = joinVar2;
    }

    @Override
    public boolean add( final SolutionMapping e ) {
        final Binding solMapBinding = e.asJenaBinding();
        final Node n1 = solMapBinding.get(joinVar1);
        final Node n2 = solMapBinding.get(joinVar2);

        if (n1 == null || n2 == null){
            throw new IllegalArgumentException();
        }
        else{
            List<Node> varList = mapNN.get(n1);
            if( varList == null){
                varList = new ArrayList<>();
                mapNN.put(n1, varList);
            }
            varList.add(n2);

            List<SolutionMapping> solMapList = mapNS.get(n2);
            if ( solMapList == null ){
                solMapList = new ArrayList<>();
                mapNS.put(n2, solMapList);
            }
            solMapList.add(e);
            return true;
        }
    }

    @Override
    public Iterable<SolutionMapping> getJoinPartners( final SolutionMapping sm )
            throws UnsupportedOperationException
    {
        final Binding solMapBinding = sm.asJenaBinding();
        final Node n1 = solMapBinding.get(joinVar1);
        final Node n2 = solMapBinding.get(joinVar2);
        Iterator<SolutionMapping> matchingSolMaps;

        if ( n1 == null || n2 == null ){
            matchingSolMaps = iterator();
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

    protected Iterable<SolutionMapping> findSolMappingList(
            final Node keyValue1, final Node keyValue2 )
            throws UnsupportedOperationException
    {
        final List<Node> matchingNode = mapNN.get(keyValue1);
        if (matchingNode == null) {
            return null;
        }
        else if(matchingNode.contains(keyValue2)){
            List<SolutionMapping> solMapList = mapNS.get(keyValue2);
            return solMapList;
        }
        else{
            throw new IllegalStateException();
        }
    }
}