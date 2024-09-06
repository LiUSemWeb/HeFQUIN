package se.liu.ida.hefquin.service;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.apache.commons.cli.*;

public class HeFQUINServer {

	public static void main( String[] args ) throws Exception {
		final Options options = new Options();
		final Option arg1 = new Option( "f", "federationDescription", true, "" );
		options.addOption( arg1 );
		final Option arg2 = new Option( "c", "confDescr", true, "" );
		options.addOption( arg2 );
		final Option arg3 = new Option( "p", "port", true, "" );
		options.addOption( arg3 );

		final CommandLineParser parser = new DefaultParser();
		final CommandLine cmd;
		cmd = parser.parse( options, args );

		final String ENGINE_CONF_FILE = cmd.getOptionValue( "confDescr", "DefaultEngineConf.ttl" );
		final String FED_CONF_FILE = cmd.getOptionValue( "federationDescription", "DefaultEngineConf.ttl" );
		final int port = Integer.parseInt( cmd.getOptionValue( "port", "8080" ) );
		System.setProperty( "hefquin.configuration", ENGINE_CONF_FILE );
		System.setProperty( "hefquin.federation", FED_CONF_FILE );

		final Server server = run( port );
		server.start();
		server.join();
	}

	public static Server run( final int port ) {
		final Server server = new Server( port );
		final WebAppContext webAppContext = new WebAppContext();
		webAppContext.setResourceBase( "hefquin-service/src/main/webapp" );
		server.setHandler( webAppContext );

		return server;
	}
}
