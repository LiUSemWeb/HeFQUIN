package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;

/**
 * Command-line argument module for specifying endpoint and authentication.
 */
public class ModServer extends ModBase
{
	protected final ArgDecl argPort = new ArgDecl( ArgDecl.HasValue, "port" );
	protected final ArgDecl argConfDescr = new ArgDecl( ArgDecl.HasValue, "configurationDescription", "confDescr" );
	protected final ArgDecl argFedDescr = new ArgDecl( ArgDecl.HasValue, "federationDescription", "fd" );

	protected int port;
	protected String fedDescr;
	protected String confDescr;

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory( "Settings" );

		cmdLine.add( argPort, "--port", "Server port (default: 8080)" );
		cmdLine.add( argConfDescr, "--confDescr",
				"File with an RDF description of the configuration (default: config/DefaultConfDescr.ttl)" );
		cmdLine.add( argFedDescr, "--federationDescription",
				"File with an RDF description of the federation (default: config/DefaultFedConf.ttl)" );
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		if ( cmdLine.contains( argPort ) ) {
			port = Integer.parseInt(cmdLine.getValue( argPort ));
		} else {
			port = 8080;
		}
		if ( cmdLine.contains( argConfDescr ) ) {
			confDescr = cmdLine.getValue( argConfDescr );
		} else {
			confDescr = "config/DefaultConfDescr.ttl";
		}
		if ( cmdLine.contains( argFedDescr ) ) {
			fedDescr = cmdLine.getValue( argFedDescr );
		} else {
			fedDescr = "config/DefaultFedConf.ttl";
		}
	}

	public int getPort() {
		return port;
	}

	public String getConfDescr() {
		return confDescr;

	}

	public String getFederationDescription() {
		return fedDescr;
	}
}
