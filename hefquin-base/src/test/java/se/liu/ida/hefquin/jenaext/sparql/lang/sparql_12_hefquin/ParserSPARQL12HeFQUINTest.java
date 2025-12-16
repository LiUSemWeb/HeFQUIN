package se.liu.ida.hefquin.jenaext.sparql.lang.sparql_12_hefquin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.lang.SPARQLParser;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementService;
import org.junit.Test;

import se.liu.ida.hefquin.jenaext.sparql.syntax.ElementServiceWithParams;

public class ParserSPARQL12HeFQUINTest
{
	@Test
	public void acceptPARAMSwithOneVar() {
		final String queryString = "SELECT * { SERVICE <http://exmpl.org> PARAMS(?v) {} }";

		final SPARQLParser p = new ParserSPARQL12HeFQUIN();
		final Query q = p.parse( new Query(), queryString );

		assertTrue( q.getQueryPattern() instanceof ElementGroup );

		final ElementGroup eg = (ElementGroup) q.getQueryPattern();
		assertEquals( 1, eg.size() );
		assertTrue( eg.get(0) instanceof ElementService );
		assertTrue( eg.get(0) instanceof ElementServiceWithParams );

		final ElementServiceWithParams es = (ElementServiceWithParams) eg.get(0);
		final List<Var> paramVars = es.getParamVars();
		assertNotNull( paramVars );
		assertEquals( 1, paramVars.size() );
		assertEquals( "v", paramVars.get(0).getVarName() );
	}

	@Test
	public void acceptPARAMSwithComma() {
		final String queryString = "SELECT * { SERVICE <http://exmpl.org> PARAMS(?v1, ?v2) {} }";

		final SPARQLParser p = new ParserSPARQL12HeFQUIN();
		final Query q = p.parse( new Query(), queryString );

		final ElementGroup eg = (ElementGroup) q.getQueryPattern();
		final ElementServiceWithParams es = (ElementServiceWithParams) eg.get(0);

		final List<Var> paramVars = es.getParamVars();
		assertNotNull( paramVars );
		assertEquals( 2, paramVars.size() );
		assertEquals( "v1", paramVars.get(0).getVarName() );
		assertEquals( "v2", paramVars.get(1).getVarName() );
	}

	@Test
	public void acceptPARAMSwithoutComma() {
		final String queryString = "SELECT * { SERVICE <http://exmpl.org> PARAMS(?v1 ?v2) {} }";

		final SPARQLParser p = new ParserSPARQL12HeFQUIN();
		final Query q = p.parse( new Query(), queryString );

		final ElementGroup eg = (ElementGroup) q.getQueryPattern();
		final ElementServiceWithParams es = (ElementServiceWithParams) eg.get(0);

		final List<Var> paramVars = es.getParamVars();
		assertNotNull( paramVars );
		assertEquals( 2, paramVars.size() );
		assertEquals( "v1", paramVars.get(0).getVarName() );
		assertEquals( "v2", paramVars.get(1).getVarName() );
	}

	@Test(expected = QueryParseException.class)
	public void rejectEmptyPARAMS() {
		final String queryString = "SELECT * { SERVICE <http://exmpl.org> PARAMS() {} }";

		final SPARQLParser p = new ParserSPARQL12HeFQUIN();
		p.parse( new Query(), queryString );
	}

	@Test
	public void acceptSERVICEwithoutPARAMS() {
		final String queryString = "SELECT * { SERVICE <http://exmpl.org> {} }";

		final SPARQLParser p = new ParserSPARQL12HeFQUIN();
		final Query q = p.parse( new Query(), queryString );

		assertTrue( q.getQueryPattern() instanceof ElementGroup );

		final ElementGroup eg = (ElementGroup) q.getQueryPattern();
		assertEquals( 1, eg.size() );
		assertTrue( eg.get(0) instanceof ElementService );
		assertFalse( eg.get(0) instanceof ElementServiceWithParams );
	}
}
