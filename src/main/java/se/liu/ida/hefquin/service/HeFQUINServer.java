package se.liu.ida.hefquin.service;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class HeFQUINServer {

	public static void main( String[] args ) throws Exception {
		final Server server = run( 8080 );
		server.start();
		server.join();
	}

	public static Server run( final int port ) {
		final Server server = new Server( port );
		final ServletContextHandler context = new ServletContextHandler();
		context.setContextPath( "/" );

		final ServletHolder servlet = new ServletHolder( new HeFQUINServlet() );
		context.addServlet( servlet, "/" );
		server.setHandler( context );
		return server;
	}
}
