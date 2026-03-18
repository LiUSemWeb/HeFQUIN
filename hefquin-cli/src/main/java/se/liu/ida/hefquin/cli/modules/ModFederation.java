package se.liu.ida.hefquin.cli.modules;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotNotFoundException;

public class ModFederation extends ModBase
{
	protected final ArgDecl fedDescrDecl   = new ArgDecl(ArgDecl.HasValue, "federationDescription", "fd");

	protected List<Model> cat = null;

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory("Federation");
		cmdLine.add(fedDescrDecl,  "--federationDescription",  "file with an RDF description of the federation (can be given multiple times if your description is distributed across multiple files)");
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		if ( cmdLine.contains(fedDescrDecl) ) {			
			final List<String> filenames = cmdLine.getValues(fedDescrDecl);
			cat = new ArrayList<>();
			
			for ( final String filename : filenames ) {
				try {
					cat.add( RDFDataMgr.loadModel(filename) );
				}
				catch ( final RiotNotFoundException e ) {
					cmdLine.cmdError("Error loading federation description file: " + filename, false);
				}
			}
		}
		else {
			cmdLine.cmdError("No federation description file", false);
		}
	}

	public List<Model> getFederationCatalog() {
		return cat;
	}
}
