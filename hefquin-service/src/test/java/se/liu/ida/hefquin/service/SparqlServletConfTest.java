package se.liu.ida.hefquin.service;

import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.engine.main.QC;
import org.eclipse.jetty.server.Server;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SparqlServletConfTest {
	private static int port = 4567;

	@Before
	public void reset(){
		System.clearProperty("hefquin.configuration");
		System.clearProperty("hefquin.federation");
	}

	@Test
	public void runWithDefault() throws Exception {
		final Server server = TestServer.run(port);
		server.start();
		QC.setFactory( ARQ.getContext(), null );
		server.stop();
	}

	@Test
	public void runWithConfDescr() throws Exception {
		System.setProperty("hefquin.configuration", "config/DefaultConfDescr.ttl");
		final Server server = TestServer.run(port);
		server.start();
		QC.setFactory( ARQ.getContext(), null );
		server.stop();
	}

	@Test
	public void runWithMissingConfDescr() throws Exception {
		System.setProperty("hefquin.configuration", "InvalidConfDescr.ttl");
		final Server server = TestServer.run(port);
		try {
			server.start();
			QC.setFactory( ARQ.getContext(), null );
		    server.stop();
            fail("Expected RuntimeException: Failed to access: InvalidConfDescr.ttl");
        }
        catch ( final RuntimeException e ) {
			assertEquals( "Failed to access: InvalidConfDescr.ttl", e.getMessage() );
        }
	}

	@Test
	public void runWithFedConf() throws Exception {
		System.setProperty("hefquin.federation", "config/DefaultFedConf.ttl");
		final Server server = TestServer.run(port);
		server.start();
		QC.setFactory( ARQ.getContext(), null );
		server.stop();
	}

	@Test
	public void runWithMissingFedConf() throws Exception {
		System.setProperty("hefquin.federation", "InvalidFedConf.ttl");
		final Server server = TestServer.run(port);
		try {
			server.start();
			QC.setFactory( ARQ.getContext(), null );
		    server.stop();
            fail("Expected RuntimeException: Failed to access: InvalidFedConf.ttl");
        }
        catch ( final RuntimeException e ) {
			assertEquals( "Failed to access: InvalidFedConf.ttl", e.getMessage() );
			e.printStackTrace();
		}
	}
}
