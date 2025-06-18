package se.liu.ida.hefquin.base.data.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.junit.Test;

public class SolutionMappingUtilsTest
{
	@Test
	public void includedIn_WithIncludingSolMap() {
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final Node uri1 = NodeFactory.createURI("http://example.org/1");
		final Node uri2 = NodeFactory.createURI("http://example.org/2");

		final Binding sm1 = BindingFactory.binding( v1, uri1 );
		final Binding sm2 = BindingFactory.binding( v1, uri1, v2, uri2 );

		final boolean result = SolutionMappingUtils.includedIn(sm1, sm2);

		assertTrue(result);
	}

	@Test
	public void includedIn_WithNonIncludingSolMap1() {
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final Node uri1 = NodeFactory.createURI("http://example.org/1");
		final Node uri2 = NodeFactory.createURI("http://example.org/2");

		final Binding sm1 = BindingFactory.binding( v1, uri1 );
		final Binding sm2 = BindingFactory.binding( v1, uri2, v2, uri1 );

		final boolean result = SolutionMappingUtils.includedIn(sm1, sm2);

		assertFalse(result);
	}

	@Test
	public void includedIn_WithNonIncludingSolMap2() {
		final Var v1 = Var.alloc("v1");

		final Node uri1 = NodeFactory.createURI("http://example.org/1");
		final Node uri2 = NodeFactory.createURI("http://example.org/2");

		final Binding sm1 = BindingFactory.binding( v1, uri1 );
		final Binding sm2 = BindingFactory.binding( v1, uri2 );

		final boolean result = SolutionMappingUtils.includedIn(sm1, sm2);

		assertFalse(result);
	}

	@Test
	public void includedIn_WithEqualSolMap() {
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final Node uri1 = NodeFactory.createURI("http://example.org/1");
		final Node uri2 = NodeFactory.createURI("http://example.org/2");

		final Binding sm1 = BindingFactory.binding( v1, uri1, v2, uri2 );
		final Binding sm2 = BindingFactory.binding( v1, uri1, v2, uri2 );

		final boolean result = SolutionMappingUtils.includedIn(sm1, sm2);

		assertFalse(result);
	}

	@Test
	public void createValuesClauseOneVarOneSolMap() {
		final Var v1 = Var.alloc("v1");

		final Binding sm1 = BindingFactory.binding( v1, NodeFactory.createURI("http://example.org") );

		final List<Binding> solmaps = new ArrayList<>();
		solmaps.add(sm1);

		final String values = SolutionMappingUtils.createValuesClause( solmaps, new SerializationContext() );
		assertEquals("?v1 { <http://example.org> }", values);
	}


	@Test
	public void createValuesClauseOneVarTwoSolMaps() {
		final Var v1 = Var.alloc("v1");

		final Binding sm1 = BindingFactory.binding( v1, NodeFactory.createURI("http://example.org") );
		final Binding sm2 = BindingFactory.binding( v1, NodeFactory.createLiteral("test","en") );

		final List<Binding> solmaps = new ArrayList<>();
		solmaps.add(sm1);
		solmaps.add(sm2);

		final String values = SolutionMappingUtils.createValuesClause( solmaps, new SerializationContext() );
		assertEquals("?v1 { <http://example.org> \"test\"@en }", values);
	}

	@Test
	public void createValuesClauseTwoVarsOneSolMap() {
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final Binding sm1 = BindingFactory.binding(
				v1, NodeFactory.createURI("http://example.org"),
				v2, NodeFactory.createURI("http://example2.org") );

		final List<Binding> solmaps = new ArrayList<>();
		solmaps.add(sm1);

		final String values = SolutionMappingUtils.createValuesClause( solmaps, new SerializationContext() );
		assertEquals("( ?v1 ?v2 ) { ( <http://example.org> <http://example2.org> ) }", values);
	}

}
