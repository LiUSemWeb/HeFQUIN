package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;

import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.federation.catalog.FederationDescriptionReader;

public class ModFederation extends ModBase
{
	protected final ArgDecl fedDescrDecl   = new ArgDecl(ArgDecl.HasValue, "federationDescription", "fd");

	protected FederationCatalog cat = null; 

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory("Federation");
		cmdLine.add(fedDescrDecl,  "--federationDescription",  "file with an RDF description of the federation");
	}

	@Override
    public void processArgs(final CmdArgModule cmdLine) {
		if ( cmdLine.contains(fedDescrDecl) ) {
			final String fedDescrFilename = cmdLine.getValue(fedDescrDecl);
			cat = FederationDescriptionReader.readFromFile(fedDescrFilename);
		} else {
			cmdLine.cmdError("No federation description file") ;
		}
    }

	public FederationCatalog getFederationCatalog() {
		return cat;
	}

}
