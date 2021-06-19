package se.liu.ida.hefquin.engine.data.utils;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

import java.util.*;

import static org.junit.Assert.*;

public class SolutionMappingsIterableOverCollectionOfListsTest {
    @Test
    public void hashTableWithOneInputVariable() {
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");

        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node y2 = NodeFactory.createURI("http://example.org/y2");
        final Node z1 = NodeFactory.createURI("http://example.org/z1");
        final Node z2 = NodeFactory.createURI("http://example.org/z2");
        final Node z3 = NodeFactory.createURI("http://example.org/z3");

        final Map<Node, List<SolutionMapping>> map = new HashMap<>();

        final List<SolutionMapping> solMapList1 = new ArrayList<>();
        solMapList1.add(SolutionMappingUtils.createSolutionMapping(var2, y1, var3, z1));
        solMapList1.add(SolutionMappingUtils.createSolutionMapping(var2, y1, var3, z2));
        map.put(y1, solMapList1);

        final List<SolutionMapping> solMapList2 = new ArrayList<>();
        solMapList2.add(SolutionMappingUtils.createSolutionMapping(var2, y2, var3, z3));
        map.put(y2, solMapList2);

        // Iterate over all solution mappings contained in a collection
        final Iterable<SolutionMapping> allSolMap = new SolutionMappingsIterableOverCollectionOfLists( map.values() );
        final Iterator<SolutionMapping> it3 = allSolMap.iterator();

        assertTrue( it3.hasNext() );
        final Binding bIt31 = it3.next().asJenaBinding();
        assertEquals( 2, bIt31.size() );

        assertTrue( it3.hasNext() );
        final Binding bIt32 = it3.next().asJenaBinding();
        assertEquals( 2, bIt32.size() );

        assertTrue( it3.hasNext() );
        final Binding bIt33 = it3.next().asJenaBinding();
        assertEquals( 2, bIt33.size() );

        assertFalse( it3.hasNext() );
    }
}
