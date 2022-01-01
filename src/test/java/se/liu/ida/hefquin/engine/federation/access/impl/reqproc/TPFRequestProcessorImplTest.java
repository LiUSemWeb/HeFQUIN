package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.TPFInterface;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.TPFInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;

public class TPFRequestProcessorImplTest extends EngineTestBase
{
	@Test
	public void performRequestWithPatternOnDBpedia() throws FederationAccessException {
//		if ( skipLiveWebTests ) { return; }

		// setting up
		final Node s = NodeFactory.createURI("http://dbpedia.org/resource/Berlin");
		final Node p = NodeFactory.createURI("http://dbpedia.org/property/name");
		final Node o = NodeFactory.createVariable("o");
		final TriplePattern tp = new TriplePatternImpl(s,p,o);
		final int pageNumber = 0;
		final TPFRequest req = new TPFRequestImpl(tp, pageNumber);

		final String       tpfServerBaseURL = "http://fragments.dbpedia.org/2016-04/en";
		final TPFInterface tpfServerIface   = new TPFInterfaceImpl(tpfServerBaseURL, "subject", "predicate", "object");
		final TPFServer    tpfServer        = new TPFServer() {
			@Override public TPFInterface getInterface() { return tpfServerIface; }
		};

		final TPFRequestProcessor proc = new TPFRequestProcessorImpl();

		// performing the tested operation
		final TPFResponse resp = proc.performRequest(req, tpfServer);

		// checking
		assertEquals( req, resp.getRequest() );
		assertEquals( tpfServer, resp.getFederationMember() );
		for ( final Triple t : resp.getPayload() ) {
			final org.apache.jena.graph.Triple tt = t.asJenaTriple();
			if ( s.isConcrete() ) { assertTrue( tt.getSubject().matches(s) ); }
			if ( p.isConcrete() ) { assertTrue( tt.getPredicate().matches(p) ); }
			if ( o.isConcrete() ) { assertTrue( tt.getObject().matches(o) ); }
		}
		System.out.println( resp.getSize() );
		System.out.println( resp.getPayloadSize() );
		System.out.println( resp.getMetadataSize() );
	}

}
