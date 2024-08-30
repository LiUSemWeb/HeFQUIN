package se.liu.ida.hefquin.base.data.mappings.impl;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

public class TermMappingImplTest
{
	@Test
	public void testConstructorWithSingleLocal() {
		final Node t = NodeFactory.createURI("http://example.org/t");
		final Node g = NodeFactory.createURI("http://example.org/g");
		final Node l = NodeFactory.createURI("http://example.org/l");

		final TermMappingImpl mapping = new TermMappingImpl(t, g, l);

		assertEquals( t, mapping.type );
		assertEquals( g, mapping.globalTerm );

		final Set<Node> expectedLocals = new HashSet<>();
		expectedLocals.add(l);

		assertEquals( expectedLocals, mapping.localTerms );
	}

	@Test
	public void testConstructorWithMultipleLocals() {
		final Node t = NodeFactory.createURI("http://example.org/t");
		final Node g = NodeFactory.createURI("http://example.org/g");
		final Node l1 = NodeFactory.createURI("http://example.org/l1");
		final Node l2 = NodeFactory.createURI("http://example.org/l2");

		final TermMappingImpl mapping = new TermMappingImpl(t, g, l1, l2);

		assertEquals( t, mapping.type );
		assertEquals( g, mapping.globalTerm );

		final Set<Node> expectedLocals = new HashSet<>();
		expectedLocals.add(l1);
		expectedLocals.add(l2);

		assertEquals( expectedLocals, mapping.localTerms );
	}

	@Test
	public void testConstructorWithLocalsAsSet() {
		final Node t = NodeFactory.createURI("http://example.org/t");
		final Node g = NodeFactory.createURI("http://example.org/g");

		final Set<Node> locals = new HashSet<>();
		locals.add( NodeFactory.createURI("http://example.org/l1") );
		locals.add( NodeFactory.createURI("http://example.org/l1") );

		final TermMappingImpl mapping = new TermMappingImpl(t, g, locals);

		assertEquals( t, mapping.type );
		assertEquals( g, mapping.globalTerm );
		assertEquals( locals, mapping.localTerms );
	}
}
