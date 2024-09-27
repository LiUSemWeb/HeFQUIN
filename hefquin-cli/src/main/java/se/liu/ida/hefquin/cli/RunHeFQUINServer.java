package se.liu.ida.hefquin.cli;

import java.io.File;

import org.apache.jena.cmd.CmdGeneral;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import se.liu.ida.hefquin.cli.modules.ModServer;

public class RunHeFQUINServer extends CmdGeneral
{
	protected final ModServer modServer = new ModServer();

	/**
	 * Main entry point of the tool, accepting command-line arguments to specify the
	 * system configuration.
	 *
	 * @param argv Command-line arguments.
	 */
	public static void main( final String[] args ) {
		new RunHeFQUINServer( args ).mainRun();
	}

	/**
	 * Constructor that initializes the command-line tool with necessary argument
	 * modules for specifying, e.g., federation configuration and engine
	 * configuration.
	 * 
	 * @param argv Command-line arguments.
	 */
	public RunHeFQUINServer( final String[] argv ) {
		super( argv );
		addModule( modServer );
	}

	/**
	 * Returns the usage summary string of the command.
	 *
	 * @return A string that describes the usage of the command.
	 */
	@Override
	protected String getSummary() {
		return getCommandName()
				+ " [--port=<port>] [--federationDescription=<federation-description>] [--confDescr=<engine-configuration>]";
	}

	/**
	 * Returns the command name used to invoke the tool.
	 *
	 * @return The name of the command.
	 */
	@Override
	protected String getCommandName() {
		return "hefquin-server";
	}

	/**
	 * Starts up the HeFQUIN servlet using the embedded Jetty server.
	 */
	@Override
	protected void exec() {
		System.setProperty( "hefquin.configuration", modServer.getConfDescr() );
		System.setProperty( "hefquin.federation", modServer.getFederationDescription() );

		final Server server = run( modServer.getPort() );
		try {
			server.start();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		try {
			server.join();
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}
	}

	public static Server run( final int port ) {
		final Server server = new Server( port );
		System.out.println("Running on: http://localhost:" + port);
		final WebAppContext webAppContext = new WebAppContext();

		final File webAppDir = new File("hefquin-service/src/main/webapp");
		if ( webAppDir.exists() ) {
			// Running from source (for development)
			webAppContext.setResourceBase(webAppDir.getAbsolutePath());
		} else {
			// Running from a JAR - load the webapp directory from the classpath
			String webappPath = RunHeFQUINServer.class.getClassLoader().getResource("webapp").toExternalForm();
			webAppContext.setResourceBase(webappPath);
		}

		server.setHandler( webAppContext );

		return server;
	}

}
