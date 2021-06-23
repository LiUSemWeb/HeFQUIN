package se.liu.ida.hefquin.engine.datastructures.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This is a class with helper functions for testing SolutionMappingsIndex
 */
public class TestsForSolutionMappingsIndex
{
	final Var var1 = Var.alloc("v1");
	final Var var2 = Var.alloc("v2");
	final Var var3 = Var.alloc("v3");
	final Var var4 = Var.alloc("v4");

	final Node x1 = NodeFactory.createURI("http://example.org/x1");
	final Node x2 = NodeFactory.createURI("http://example.org/x2");
	final Node x3 = NodeFactory.createURI("http://example.org/x3");
	final Node y1 = NodeFactory.createURI("http://example.org/y1");
	final Node y2 = NodeFactory.createURI("http://example.org/y2");
	final Node y3 = NodeFactory.createURI("http://example.org/y3");
	final Node z1 = NodeFactory.createURI("http://example.org/z1");
	final Node z2 = NodeFactory.createURI("http://example.org/z2");
	final Node z3 = NodeFactory.createURI("http://example.org/z3");
	final Node p = NodeFactory.createURI("http://example.org/p");

	protected SolutionMappingsIndexBase createHashTableBasedOneVar()
	{
		final SolutionMappingsIndexBase solMHashTable = new SolutionMappingsHashTableBasedOnOneVar(var2);
		solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
				var2, y1,
				var3, z1) );
		solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
				var2, y1,
				var3, z2) );
		solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
				var2, y2,
				var3, z3) );

		return solMHashTable;
	}

	protected SolutionMappingsIndexBase createHashTableBasedTwoVars()
	{
		final SolutionMappingsIndexBase solMHashTable = new SolutionMappingsHashTableBasedOnTwoVars(var1, var2);
		solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1,
				var3, z1) );
		solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1,
				var3, z2) );
		solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
				var1, x2,
				var2, y2,
				var3, z2) );

		return solMHashTable;
	}

	protected SolutionMappingsIndexBase createHashTableBasedThreeVars()
	{
		final SolutionMappingsHashTable solMHashTable = new SolutionMappingsHashTable(var1, var2, var3);
		solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1,
				var3, z1) );
		solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1,
				var3, z2) );
		solMHashTable.add( SolutionMappingUtils.createSolutionMapping(
				var1, x2,
				var2, y2,
				var3, z2) );

		return solMHashTable;
	}

	protected void assertHasNext( final Iterator<SolutionMapping> it,
								  final String expectedURIforV1, final Var v1,
								  final String expectedURIforV2, final Var v2 )
	{
		assertTrue( it.hasNext() );

		final Binding b = it.next().asJenaBinding();
		assertEquals( 2, b.size() );

		assertEquals( expectedURIforV1, b.get(v1).getURI() );
		assertEquals( expectedURIforV2, b.get(v2).getURI() );
	}

	protected void assertHasNext( final Iterator<SolutionMapping> it,
								  final String expectedURIforV1, final Var v1,
								  final String expectedURIforV2, final Var v2,
								  final String expectedURIforV3, final Var v3 )
	{
		assertTrue( it.hasNext() );

		final Binding b = it.next().asJenaBinding();
		assertEquals( 3, b.size() );

		assertEquals( expectedURIforV1, b.get(v1).getURI() );
		assertEquals( expectedURIforV2, b.get(v2).getURI() );
		assertEquals( expectedURIforV3, b.get(v3).getURI() );
	}

}
