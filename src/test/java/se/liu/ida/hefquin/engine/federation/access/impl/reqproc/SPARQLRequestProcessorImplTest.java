package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.query.impl.SPARQLQueryImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;

public class SPARQLRequestProcessorImplTest extends EngineTestBase
{
	@Test
	public void performRequestWithPatternOnDBpedia() throws FederationAccessException {
		if ( ! skipLiveWebTests ) {
			// setting up
			final String queryString = "SELECT * WHERE { <http://dbpedia.org/resource/Berlin> <http://xmlns.com/foaf/0.1/name> ?o }";
			final SPARQLGraphPattern pattern = new SPARQLGraphPatternImpl( QueryFactory.create(queryString).getQueryPattern() );
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


	// ------------ helper code ------------

	protected Iterator<SolutionMapping> performRequestOnDBpediaHelper(
			final SPARQLRequest req )
					throws FederationAccessException
	{
		final SPARQLEndpoint fm = new SPARQLEndpointForTest("http://dbpedia.org/sparql");

		final SPARQLRequestProcessor recProc = new SPARQLRequestProcessorImpl();

		// executing the tested method
		final SolMapsResponse resp = recProc.performRequest(req, fm);

		// checking
		assertEquals( fm, resp.getFederationMember() );
		assertEquals( req, resp.getRequest() );

		return resp.getSolutionMappings().iterator();
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
