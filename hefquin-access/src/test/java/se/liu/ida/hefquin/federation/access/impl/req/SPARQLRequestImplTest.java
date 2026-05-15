package se.liu.ida.hefquin.federation.access.impl.req;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLQuery;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.SPARQLQueryImpl;
import se.liu.ida.hefquin.federation.FederationTestBase;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;

public class SPARQLRequestImplTest extends FederationTestBase
{
	@Test
	public void testDistinctRequestInitialization() throws FederationAccessException {
		// setting up
		final String queryString = "SELECT ?s WHERE { ?s <http://xmlns.com/foaf/0.1/name> \"Berlin\"@en . ?s <http://xmlns.com/foaf/0.1/name> ?o2 .}";
		final SPARQLQuery baseQuery = new SPARQLQueryImpl( QueryFactory.create(queryString) );

		final SPARQLRequest req1 = new SPARQLRequestImpl(baseQuery);

		final Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl1(el);
		final SPARQLRequest req2 = new SPARQLRequestImpl(pattern, Set.of(Var.alloc("s")), true);

		// checking
		// the base query does not contain DISTINCT
		assertFalse( baseQuery.asJenaQuery().isDistinct() );

		// req1 inherits DISTINCT=false, req2 explicitly enforces DISTINCT
		assertFalse( req1.getDistinctRequired() );
		assertTrue( req2.getDistinctRequired() );
	}

	@Test
	public void testProjectionVariablesInitialization() throws FederationAccessException {
		// setting up
		final String queryString = "SELECT ?s ?o2 WHERE { ?s <http://xmlns.com/foaf/0.1/name> \"Berlin\"@en . " +
									"?s <http://xmlns.com/foaf/0.1/name> ?o2 . }";

		final SPARQLQuery baseQuery = new SPARQLQueryImpl( QueryFactory.create(queryString) );

		final SPARQLRequest req1 = new SPARQLRequestImpl(baseQuery);

		final Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl1(el);
		final SPARQLRequest req2 = new SPARQLRequestImpl(pattern, Set.of(Var.alloc("s")), false);

		// checking
		// ensure projection is applied
		assertEquals( Set.of(Var.alloc("s"), Var.alloc("o2")), req1.getProjectionVars() );
		assertEquals( Set.of(Var.alloc("s")), req2.getProjectionVars() );
	}
}
