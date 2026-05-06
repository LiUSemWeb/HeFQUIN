package se.liu.ida.hefquin.federation.access.impl.req;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLQuery;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.SPARQLQueryImpl;
import se.liu.ida.hefquin.federation.FederationTestBase;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.SPARQLRequestProcessorImpl;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;

public class SPARQLRequestImplTest extends FederationTestBase
{
	@Test
	public void performRequestWithQueryOnDBpediaDistinct() throws FederationAccessException {
		if ( ! skipLiveWebTests ) {
			// setting up
			final String queryString = "SELECT ?s WHERE { ?s <http://xmlns.com/foaf/0.1/name> \"Berlin\"@en . ?s <http://xmlns.com/foaf/0.1/name> ?o2 .}";
			final SPARQLQuery baseQuery = new SPARQLQueryImpl( QueryFactory.create(queryString) );

			final SPARQLRequest req1 = new SPARQLRequestImpl(baseQuery);

			final Element el = QueryFactory.create(queryString).getQueryPattern();
			final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl1(el);
			final SPARQLRequest req2 = new SPARQLRequestImpl(pattern, Set.of(Var.alloc("s")), true);

			// performing the tested operation
			final SPARQLEndpoint fm = new SPARQLEndpointForTest("http://dbpedia.org/sparql");

			final SPARQLRequestProcessor recProc = new SPARQLRequestProcessorImpl();

			final SolMapsResponse resp1 = recProc.performRequest(req1, fm);
			final SolMapsResponse resp2 = recProc.performRequest(req2, fm);

			// checking
			// the base query does not contain DISTINCT
			assertFalse( baseQuery.asJenaQuery().isDistinct() );

			// req1 inherits DISTINCT=false, req2 explicitly enforces DISTINCT
			assertFalse( req1.isDistinct() );
			assertTrue( req2.isDistinct() );

			// ensure the endpoint actually returned data
			assertTrue( resp2.getResponseData().iterator().hasNext() );

			// DISTINCT should not increase result size
			assertTrue( resp1.getSize() > resp2.getSize() );

			// DISTINCT results must be a subset of the non-DISTINCT results
			Set<SolutionMapping> resp1Set = new HashSet<>();
			resp1.getResponseData().forEach( resp1Set::add );
			resp2.getResponseData().forEach( sm -> assertTrue( resp1Set.contains(sm) ) );

			// verify that the non-DISTINCT result actually contained duplicates
			assertTrue( resp1.getSize() > resp1Set.size() );

			// verify that DISTINCT removed all duplicates
			Set<SolutionMapping> unique = new HashSet<>();
			resp2.getResponseData().forEach( unique::add );
			assertEquals( unique.size(), resp2.getSize() );
		}
	}

	@Test
	public void performRequestWithQueryOnDBpediaProject() throws FederationAccessException {
		if ( ! skipLiveWebTests ) {
			// setting up
			final String queryString = "SELECT ?s ?o2 WHERE { ?s <http://xmlns.com/foaf/0.1/name> \"Berlin\"@en . " +
			                           "?s <http://xmlns.com/foaf/0.1/name> ?o2 . }";

			final SPARQLQuery baseQuery = new SPARQLQueryImpl( QueryFactory.create(queryString) );

			final SPARQLRequest req1 = new SPARQLRequestImpl(baseQuery);

			final Element el = QueryFactory.create(queryString).getQueryPattern();
			final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl1(el);
			final SPARQLRequest req2 = new SPARQLRequestImpl(pattern, Set.of(Var.alloc("s")), false);

			// performing the tested operation
			final SPARQLEndpoint fm = new SPARQLEndpointForTest("http://dbpedia.org/sparql");

			final SPARQLRequestProcessor recProc = new SPARQLRequestProcessorImpl();

			final SolMapsResponse resp1 = recProc.performRequest(req1, fm);
			final SolMapsResponse resp2 = recProc.performRequest(req2, fm);

			// checking
			// ensure projection is applied
			for ( final SolutionMapping sm : resp2.getResponseData() ) {
				assertEquals( 1, sm.asJenaBinding().size() );
				assertTrue( sm.asJenaBinding().contains( Var.alloc("s") ) );
			}

			// ensure original had more variables
			boolean foundO2 = false;
			for ( final SolutionMapping sm : resp1.getResponseData() ) {
				if ( sm.asJenaBinding().contains( Var.alloc("o2") ) ) {
					foundO2 = true;
					break;
				}
			}
			assertTrue( foundO2 );

			// ensure projection does NOT change number of rows
			assertEquals( resp1.getSize(), resp2.getSize() );
		}
	}
}
