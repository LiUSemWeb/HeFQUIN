package se.liu.ida.hefquin.engine.datastructures.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.datastructures.SolutionMappingsIndex;

/**
 * This is a class with helper functions for testing SolutionMappingsIndex
 */
public abstract class TestsForSolutionMappingsIndex extends EngineTestBase
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

	protected SolutionMappingsIndex createHashTableBasedOneVar()
	{
		final SolutionMappingsIndex solMHashTable = new SolutionMappingsHashTableBasedOnOneVar(var2);
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

	protected SolutionMappingsIndex createHashTableBasedTwoVars()
	{
		final SolutionMappingsIndex solMHashTable = new SolutionMappingsHashTableBasedOnTwoVars(var1, var2);
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

	protected SolutionMappingsIndex createHashTableBasedThreeVars()
	{
		final SolutionMappingsIndex solMHashTable = new SolutionMappingsHashTable(var1, var2, var3);
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

}
