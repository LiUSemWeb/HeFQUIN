package se.liu.ida.hefquin.base.data.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;

public class SolutionMappingUtilsTest
{
	@Test
	public void createValuesClauseOneVarOneSolMap() {
		final Var v1 = Var.alloc("v1");

		final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping( v1, NodeFactory.createURI("http://example.org") );

		final List<SolutionMapping> solmaps = new ArrayList<>();
		solmaps.add(sm1);

		final String values = SolutionMappingUtils.createValuesClause( solmaps, new SerializationContext() );
		assertEquals("?v1 { <http://example.org> }", values);
	}


	@Test
	public void createValuesClauseOneVarTwoSolMaps() {
		final Var v1 = Var.alloc("v1");

		final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping( v1, NodeFactory.createURI("http://example.org") );
		final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping( v1, NodeFactory.createLiteral("test","en") );

		final List<SolutionMapping> solmaps = new ArrayList<>();
		solmaps.add(sm1);
		solmaps.add(sm2);

		final String values = SolutionMappingUtils.createValuesClause( solmaps, new SerializationContext() );
		assertEquals("?v1 { <http://example.org> \"test\"@en }", values);
	}

	@Test
	public void createValuesClauseTwoVarsOneSolMap() {
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(
				v1, NodeFactory.createURI("http://example.org"),
				v2, NodeFactory.createURI("http://example2.org") );

		final List<SolutionMapping> solmaps = new ArrayList<>();
		solmaps.add(sm1);

		final String values = SolutionMappingUtils.createValuesClause( solmaps, new SerializationContext() );
		assertEquals("( ?v1 ?v2 ) { ( <http://example.org> <http://example2.org> ) }", values);
	}

}
