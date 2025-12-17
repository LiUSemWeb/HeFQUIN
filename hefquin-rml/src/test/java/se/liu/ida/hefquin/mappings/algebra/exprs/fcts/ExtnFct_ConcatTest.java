package se.liu.ida.hefquin.mappings.algebra.exprs.fcts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;

public class ExtnFct_ConcatTest
{
	@Test
	public void oneString() {
		final Node s = NodeFactory.createLiteralString("a");

		final Node result = new ExtnFct_Concat().apply(s);

		assertTrue( result.isLiteral() );
		assertEquals( XSDDatatype.XSDstring, result.getLiteralDatatype() );
		assertEquals( "a", result.getLiteralLexicalForm() );
	}

	@Test
	public void twoStrings() {
		final Node s1 = NodeFactory.createLiteralString("a");
		final Node s2 = NodeFactory.createLiteralString("b");

		final Node result = new ExtnFct_Concat().apply(s1, s2);

		assertTrue( result.isLiteral() );
		assertEquals( XSDDatatype.XSDstring, result.getLiteralDatatype() );
		assertEquals( "ab", result.getLiteralLexicalForm() );
	}

	@Test
	public void stringAndInteger() {
		final Node x1 = NodeFactory.createLiteralString("a");
		final Node x2 = NodeFactory.createLiteralByValue(42);

		final Node result = new ExtnFct_Concat().apply(x1, x2);

		assertTrue( result == MappingRelation.errorNode );
	}

	@Test
	public void integerAndString() {
		final Node x1 = NodeFactory.createLiteralByValue(42);
		final Node x2 = NodeFactory.createLiteralString("a");

		final Node result = new ExtnFct_Concat().apply(x1, x2);

		assertTrue( result == MappingRelation.errorNode );
	}

	@Test
	public void stringAndIRI() {
		final Node x1 = NodeFactory.createLiteralString("a");
		final Node x2 = NodeFactory.createURI("http://example.com/");

		final Node result = new ExtnFct_Concat().apply(x1, x2);

		assertTrue( result == MappingRelation.errorNode );
	}
}
