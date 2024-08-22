package se.liu.ida.hefquin.service;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class HeFQUINServer {

	public static void main( String[] args ) throws Exception {
		final Server server = run( 8080 );
		server.start();
		server.join();
	}

	public static Server run( final int port ) {
		final Server server = new Server( port );

        WebAppContext webAppContext = new WebAppContext();
		webAppContext.setResourceBase("src/main/webapp");
        server.setHandler(webAppContext);

		return server;
	}
}
