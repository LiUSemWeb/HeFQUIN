package se.liu.ida.hefquin.federation.access.impl.reqproc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.Element;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLQuery;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.BGPImpl;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.SPARQLQueryImpl;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.federation.FederationTestBase;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;

public class SPARQLRequestProcessorImplTest extends FederationTestBase
{
	@Test
	public void performRequestWithPatternOnDBpedia() throws FederationAccessException {
		if ( ! skipLiveWebTests ) {
			// setting up
			final String queryString = "SELECT * WHERE { <http://dbpedia.org/resource/Berlin> <http://xmlns.com/foaf/0.1/name> ?o }";
			final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl1( QueryFactory.create(queryString).getQueryPattern() );
			final SPARQLRequest req = new SPARQLRequestImpl(pattern);

			// performing the tested operation
			final Iterator<SolutionMapping> it = performRequestOnDBpediaHelper(req);

			// checking
			checkPatternResponse(it);
		}
	}

	@Test
	public void performRequestWithTPOnDBpedia() throws FederationAccessException {
		if ( ! skipLiveWebTests ) {
			// setting up
			final Node s = NodeFactory.createURI("http://dbpedia.org/resource/Berlin");
			final Node p = NodeFactory.createURI("http://xmlns.com/foaf/0.1/name");
			final Node o = NodeFactory.createVariable("o");
			final TriplePattern tp = new TriplePatternImpl(s,p,o);
			final SPARQLRequest req = new TriplePatternRequestImpl(tp);

			// performing the tested operation
			final Iterator<SolutionMapping> it = performRequestOnDBpediaHelper(req);

			// checking
			checkPatternResponse(it);
		}
	}

	@Test
	public void performRequestWithBGPOnDBpedia() throws FederationAccessException {
		if ( ! skipLiveWebTests ) {
			// setting up
			final Node s = NodeFactory.createURI("http://dbpedia.org/resource/Berlin");
			final Node p = NodeFactory.createURI("http://xmlns.com/foaf/0.1/name");
			final Node o = NodeFactory.createVariable("o");
			final TriplePattern tp = new TriplePatternImpl(s,p,o);
			final SPARQLRequest req = new BGPRequestImpl( new BGPImpl(tp) );

			// performing the tested operation
			final Iterator<SolutionMapping> it = performRequestOnDBpediaHelper(req);

			// checking
			checkPatternResponse(it);
		}
	}

	@Test
	public void performRequestWithQueryOnDBpedia() throws FederationAccessException {
		if ( ! skipLiveWebTests ) {
			// setting up
			final String queryString = "SELECT (COUNT(*) AS ?c) WHERE { <http://dbpedia.org/resource/Berlin> <http://xmlns.com/foaf/0.1/name> ?o }";
			final SPARQLQuery query = new SPARQLQueryImpl( QueryFactory.create(queryString) );
			final SPARQLRequest req = new SPARQLRequestImpl(query);

			// performing the tested operation
			final Iterator<SolutionMapping> it = performRequestOnDBpediaHelper(req);

			// checking
			assertTrue( it.hasNext() );

			final Binding b = it.next().asJenaBinding();
			final Var var = Var.alloc("c");
			assertEquals( 1, b.size() );
			assertTrue( b.contains(var) );

			final Node n = b.get(var);
			assertTrue( n.isLiteral() );
			assertEquals( 1, n.getLiteralValue() );
		}
	}

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
			for ( SolutionMapping sm : resp2.getResponseData() ) {
				assertEquals( 1, sm.asJenaBinding().size() );
				assertTrue( sm.asJenaBinding().contains( Var.alloc("s") ) );
			}

			// ensure original had more variables
			boolean foundO2 = false;
			for (SolutionMapping sm : resp1.getResponseData()) {
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

	// ------------ helper code ------------

	protected Iterator<SolutionMapping> performRequestOnDBpediaHelper(
			final SPARQLRequest req )
					throws FederationAccessException
	{
		final SPARQLEndpoint fm = new SPARQLEndpointForTest("http://dbpedia.org/sparql");

		final SPARQLRequestProcessor recProc = new SPARQLRequestProcessorImpl();

		// executing the tested method
		final SolMapsResponse resp = recProc.performRequest(req, fm);

		return resp.getResponseData().iterator();
	}

	protected void checkPatternResponse( final Iterator<SolutionMapping> it ) {
		assertTrue( it.hasNext() );

		final Binding b = it.next().asJenaBinding();
		final Var var = Var.alloc("o");
		assertEquals( 1, b.size() );
		assertTrue( b.contains(var) );

		final Node n = b.get(var);
		assertTrue( n.isLiteral() );
	}

}
