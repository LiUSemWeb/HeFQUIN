package se.liu.ida.hefquin.cli.modules;

import java.util.List;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;

public class ModFederation extends ModBase
{
	protected final ArgDecl fedDescrDecl   = new ArgDecl(ArgDecl.HasValue, "federationDescription", "fd");

	protected List<String> cat = null;

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory("Federation");
		cmdLine.add(fedDescrDecl,  "--federationDescription",  "file with an RDF description of the federation (can be given multiple times if your description is distributed across multiple files)");
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		if ( cmdLine.contains(fedDescrDecl) ) {
			cat = cmdLine.getValues(fedDescrDecl);
		}
		else {
			cmdLine.cmdError("No federation description file", false);
		}
	}

	public List<String> getFederationCatalog() {
		return cat;
	}
}
