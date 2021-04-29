package se.liu.ida.hefquin.federation.access.impl.jenaimpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;

import se.liu.ida.hefquin.EngineTestBase;
import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMapping;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.query.jenaimpl.JenaBasedSPARQLGraphPattern;

public class JenaBasedSPARQLRequestProcessorTest extends EngineTestBase
{
	@Test
	public void testDBpedia() {
		if ( ! skipLiveWebTests ) {
			// setting up
			final String queryString = "SELECT * WHERE { <http://dbpedia.org/resource/Berlin> <http://xmlns.com/foaf/0.1/name> ?o }";
			final SPARQLGraphPattern pattern = new JenaBasedSPARQLGraphPattern( QueryFactory.create(queryString).getQueryPattern() );
			final SPARQLRequest req = new SPARQLRequestImpl(pattern);

			final SPARQLEndpoint fm = new SPARQLEndpointForTest("http://dbpedia.org/sparql");

			final SPARQLRequestProcessor recProc = new JenaBasedSPARQLRequestProcessor();

			// executing the tested method
			final SolMapsResponse resp = recProc.performRequest(req, fm);

			// checking
			assertEquals( fm, resp.getFederationMember() );
			assertEquals( req, resp.getRequest() );

			final Iterator<SolutionMapping> it = resp.getIterator();
			assertTrue( it.hasNext() );

			final Binding b = ( (JenaBasedSolutionMapping) it.next() ).asJenaBinding();
			final Var var = Var.alloc("o");
			assertEquals( 1, b.size() );
			assertTrue( b.contains(var) );

			final Node n = b.get(var);
			assertTrue( n.isLiteral() );
		}
	}

}
