package se.liu.ida.hefquin.service;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class TestServer {

	public static void main( String[] args ) throws Exception {
		final Server server = run( 8888 );
		server.start();
		server.join();
	}

	public static Server run( final int port ) {
		final Server server = new Server( port );
		final ServletContextHandler context = new ServletContextHandler();
		context.setContextPath( "/" );
		context.addEventListener( new SharedResourceInitializer() );

		final ServletHolder servlet = new ServletHolder( new SparqlServlet() );
		context.addServlet( servlet, "/" );

		final ServletHolder inspectServlet = new ServletHolder( new InspectServlet() );
		context.addServlet( inspectServlet, "/query-inspect" );

		server.setHandler( context );
		return server;
	}
}
