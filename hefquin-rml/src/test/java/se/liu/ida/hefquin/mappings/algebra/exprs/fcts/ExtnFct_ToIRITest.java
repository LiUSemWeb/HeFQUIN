package se.liu.ida.hefquin.mappings.algebra.exprs.fcts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;

public class ExtnFct_ToIRITest
{
	@Test
	public void correctCase1() {
		final Node v = NodeFactory.createLiteralString("http://example.org/");
		final Node base = NodeFactory.createURI("http://example.com/");

		final Node result = new ExtnFct_ToIRI().apply(v, base);
		assertTrue( result.isURI() );
		assertEquals( "http://example.org/", result.getURI() );
	}

	@Test
	public void correctCase2() {
		final Node v = NodeFactory.createLiteralString("test");
		final Node base = NodeFactory.createURI("http://example.com/");

		final Node result = new ExtnFct_ToIRI().apply(v, base);
		assertTrue( result.isURI() );
		assertEquals( "http://example.com/test", result.getURI() );
	}

	@Test
	public void arg1NoLiteral() {
		final Node v = NodeFactory.createURI("http://example.org/");
		final Node base = NodeFactory.createURI("http://example.com/");

		final Node result = new ExtnFct_ToIRI().apply(v, base);
		assertTrue( result == MappingRelation.errorNode );
	}

	@Test
	public void arg1NoStringLiteral() {
		final Node v = NodeFactory.createLiteralDT("http://example.org/", XSDDatatype.XSDanyURI);
		final Node base = NodeFactory.createURI("http://example.com/");

		final Node result = new ExtnFct_ToIRI().apply(v, base);
		assertTrue( result == MappingRelation.errorNode );
	}

	@Test
	public void arg2NoURI() {
		final Node v = NodeFactory.createLiteralString("test");
		final Node base = NodeFactory.createBlankNode();

		final Node result = new ExtnFct_ToIRI().apply(v, base);
		assertTrue( result == MappingRelation.errorNode );
	}

	@Test
	public void arg2NoBaseURI() {
		final Node v = NodeFactory.createLiteralString("test");
		final Node base = NodeFactory.createURI("test");

		final Node result = new ExtnFct_ToIRI().apply(v, base);
		assertTrue( result == MappingRelation.errorNode );
	}
}
