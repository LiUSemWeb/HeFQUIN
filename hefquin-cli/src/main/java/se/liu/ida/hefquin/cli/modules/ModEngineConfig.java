package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;

public class ModEngineConfig extends ModBase
{
	protected final ArgDecl confDescrDecl   = new ArgDecl(ArgDecl.HasValue, "configurationDescription", "confDescr");

	protected String confDescr = null;

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory("Configuration options for HeFQUIN engine") ;

		cmdLine.add(confDescrDecl, "--confDescr", "file with an RDF description of the configuration");
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		if ( cmdLine.contains(confDescrDecl) ) {
			confDescr = cmdLine.getValue(confDescrDecl);
		}
	}

	public String getConfDescr() {
		return confDescr;
	}
}
