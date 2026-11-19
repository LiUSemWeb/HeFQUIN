package se.liu.ida.hefquin.federation.access.impl.req;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.util.ExprUtils;
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
		final SPARQLRequest req2 = new SPARQLRequestImpl(pattern, Set.of(Var.alloc("s")), null, true);

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
		final String queryString =
				"SELECT ?s ?o2 WHERE { " +
				    "?s <http://example.org/1> ?o1 . " +
				    "?s <http://example.org/2> ?o2 }";

		final SPARQLQuery baseQuery = new SPARQLQueryImpl( QueryFactory.create(queryString) );

		final SPARQLRequest req1 = new SPARQLRequestImpl(baseQuery);

		final Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl1(el);
		final SPARQLRequest req2 = new SPARQLRequestImpl(pattern, Set.of(Var.alloc("s")), null, false);

		// checking
		// i) ensure projection is applied
		assertEquals( Set.of(Var.alloc("s"), Var.alloc("o2")),
		              req1.getProjectionVars() );
		assertEquals( Set.of(Var.alloc("s")),
		              req2.getProjectionVars() );

		// ii) ensure that determining the expected variables considers projection
		assertEquals( Set.of(Var.alloc("s"), Var.alloc("o2")),
		              req1.getExpectedVariables().getCertainVariables() );
		assertEquals( Set.of(Var.alloc("s")),
		              req2.getExpectedVariables().getCertainVariables() );

		assertTrue( req1.getExpectedVariables().getPossibleVariables().isEmpty() );
		assertTrue( req2.getExpectedVariables().getPossibleVariables().isEmpty() );
	}

	// The test should create a SPARQLRequestImpl
	// using the constructor used by the existing tests in this class, but including some SortCondition.
	// Then, the test should get the Jena Query from the created SPARQLRequest, create the SPARQL query
	// string of this Query (there is a serialize() function in Query), and check that it looks as expected (e.g., contains the "ORDER BY" keyword).
	@Test
	public void testSortConditionInitialization() throws FederationAccessException {
		// setting up
		final String queryString =
				"SELECT ?s ?o2 WHERE { " +
					"?s <http://example.org/1> ?o1 . " +
					"?s <http://example.org/2> ?o2 } ORDER BY ASC(?o2)";

		final SPARQLQuery baseQuery = new SPARQLQueryImpl( QueryFactory.create(queryString) );
		final SPARQLRequest req1 = new SPARQLRequestImpl(baseQuery);

		final Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl1(el);
		final SPARQLRequest req2 = new SPARQLRequestImpl( pattern, null, List.of(new SortCondition(ExprUtils.parse("?o2"), Query.ORDER_DESCENDING)), false );

		// checking
		// i) ensure sort conditions are applied
		assertEquals( List.of(new SortCondition(ExprUtils.parse("?o2"), Query.ORDER_ASCENDING)),
		              req1.getSortConditions());
		assertEquals( List.of(new SortCondition(ExprUtils.parse("?o2"), Query.ORDER_DESCENDING)),
		              req2.getSortConditions());

		// ii) ensure sort conditions are incorporated into the underlying Jena query
		final String serializedQuery = req2.getQuery().asJenaQuery().serialize();
		System.out.println("Serialized query: " + serializedQuery);
		assertTrue(serializedQuery.contains("ORDER BY"));
	}
}
