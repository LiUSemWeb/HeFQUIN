package se.liu.ida.hefquin.mappings.algebra.exprs.fcts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;

public class ExtnFct_ToLiteralTest
{
	@Test
	public void incorrect1stArg() {
		final String dtURI = XSDDatatype.XSDinteger.getURI();

		final Node lex = NodeFactory.createBlankNode();
		final Node dt = NodeFactory.createURI(dtURI);

		final Node result = new ExtnFct_ToLiteral().apply(lex, dt);
		assertTrue( result == MappingRelation.errorNode );
	}

	@Test
	public void incorrect2ndArg() {
		final Node lex = NodeFactory.createLiteralString("1");
		final Node dt = NodeFactory.createBlankNode();

		final Node result = new ExtnFct_ToLiteral().apply(lex, dt);
		assertTrue( result == MappingRelation.errorNode );
	}

	@Test
	public void unknownDatatypeIRI() {
		final String dtURI = "http://example.org/";

		final Node lex = NodeFactory.createLiteralString("1");
		final Node dt = NodeFactory.createURI(dtURI);

		final Node result = new ExtnFct_ToLiteral().apply(lex, dt);
		assertTrue( result.isLiteral() );
		//assertTrue( result.getLiteral().isWellFormed() );
		assertEquals( "1", result.getLiteralLexicalForm() );
		assertEquals( dtURI, result.getLiteralDatatypeURI() );
	}

	@Test
	public void wellFormedInteger() {
		final String dtURI = XSDDatatype.XSDinteger.getURI();

		final Node lex = NodeFactory.createLiteralString("1");
		final Node dt = NodeFactory.createURI(dtURI);

		final Node result = new ExtnFct_ToLiteral().apply(lex, dt);
		assertTrue( result.isLiteral() );
		assertTrue( result.getLiteral().isWellFormed() );
		assertEquals( "1", result.getLiteralLexicalForm() );
		assertEquals( dtURI, result.getLiteralDatatypeURI() );
	}

	@Test
	public void illFormedInteger() {
		final String dtURI = XSDDatatype.XSDinteger.getURI();

		final Node lex = NodeFactory.createLiteralString("hello");
		final Node dt = NodeFactory.createURI(dtURI);

		final Node result = new ExtnFct_ToLiteral().apply(lex, dt);
		assertTrue( result.isLiteral() );
		assertFalse( result.getLiteral().isWellFormed() );
		assertEquals( "hello", result.getLiteralLexicalForm() );
		assertEquals( dtURI, result.getLiteralDatatypeURI() );
	}

	@Test
	public void wellFormedCDTList() {
		final String dtURI = CompositeDatatypeList.uri;

		final Node lex = NodeFactory.createLiteralString("[1,1]");
		final Node dt = NodeFactory.createURI(dtURI);

		final Node result = new ExtnFct_ToLiteral().apply(lex, dt);
		assertTrue( result.isLiteral() );
		assertTrue( result.getLiteral().isWellFormed() );
		assertEquals( "[1,1]", result.getLiteralLexicalForm() );
		assertEquals( dtURI, result.getLiteralDatatypeURI() );
	}

	@Test
	public void illFormedCDTList() {
		final String dtURI = CompositeDatatypeList.uri;

		final Node lex = NodeFactory.createLiteralString("hello");
		final Node dt = NodeFactory.createURI(dtURI);

		final Node result = new ExtnFct_ToLiteral().apply(lex, dt);
		assertTrue( result.isLiteral() );
		assertFalse( result.getLiteral().isWellFormed() );
		assertEquals( "hello", result.getLiteralLexicalForm() );
		assertEquals( dtURI, result.getLiteralDatatypeURI() );
	}

}
