package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;
import java.util.Set;

/**
 * Command-line argument module for specifying endpoint and authentication.
 */
public class ModEndpoint extends ModBase {
	protected final ArgDecl argEndpointURI = new ArgDecl( ArgDecl.HasValue, "endpoint", "e" );
	protected final ArgDecl argEndpointUsername = new ArgDecl( ArgDecl.HasValue, "username", "u" );
	protected final ArgDecl argEndpointPassword = new ArgDecl( ArgDecl.HasValue, "password", "p" );

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory( "Endpoint" );
		cmdLine.add( argEndpointURI, "-e   --endpoint", "The URL of the Neo4j endpoint" );
		cmdLine.add( argEndpointUsername, "-u   --username", "Username for the Neo4j endpoint" );
		cmdLine.add( argEndpointPassword, "-p   --password", "Password for the Neo4j endpoint" );
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		// Required args
		final Set<ArgDecl> requiredArgs = Set.of( argEndpointURI );
		for ( ArgDecl arg : requiredArgs ) {
			if ( ! cmdLine.contains( argEndpointURI ) ) {
				System.err.println( "Error: Missing required argument: --" + arg.getKeyName() );
				System.exit(1);
			}
		}
	}
}
