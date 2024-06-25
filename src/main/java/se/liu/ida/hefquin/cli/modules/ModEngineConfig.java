package se.liu.ida.hefquin.cli.modules;

import java.util.concurrent.ExecutorService;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;

import se.liu.ida.hefquin.engine.HeFQUINEngine;
import se.liu.ida.hefquin.engine.HeFQUINEngineConfigReader;
import se.liu.ida.hefquin.engine.HeFQUINEngineDefaultComponents;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;

public class ModEngineConfig extends ModBase
{
	protected final ArgDecl confDescrDecl   = new ArgDecl(ArgDecl.HasValue, "configurationDescription", "confDescr");

	protected Model confDescr = null;

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory("Configuration options for HeFQUIN engine") ;

		cmdLine.add(confDescrDecl, "--confDescr", "file with an RDF description of the configuration");
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		if ( cmdLine.contains(confDescrDecl) ) {
			final String filename = cmdLine.getValue(confDescrDecl);
			confDescr = RDFDataMgr.loadModel(filename);
		}
		else {
			confDescr = ModelFactory.createDefaultModel();

			final String turtle = HeFQUINEngineDefaultComponents.getDefaultConfigurationDescription();

			final RDFParserBuilder b = RDFParser.fromString(turtle);
			b.lang( Lang.TURTLE );
			b.parse(confDescr);
		}
	}

	public HeFQUINEngine getEngine( final ExecutorService execServiceForFedAccess,
	                                final ExecutorService execServiceForPlanTasks,
	                                final FederationCatalog cat,
	                                final boolean isExperimentRun,
	                                final boolean skipExecution,
	                                final boolean printSourceAssignment,
	                                final boolean printLogicalPlan,
	                                final boolean printPhysicalPlan ) {
		final HeFQUINEngineConfigReader.Context ctx = new HeFQUINEngineConfigReader.Context() {
			@Override public ExecutorService getExecutorServiceForFederationAccess() { return execServiceForFedAccess; }
			@Override public ExecutorService getExecutorServiceForPlanTasks() { return execServiceForPlanTasks; }
			@Override public FederationCatalog getFederationCatalog() { return cat; }
			@Override public boolean isExperimentRun() { return isExperimentRun; }
			@Override public boolean skipExecution() { return skipExecution; }
			@Override public boolean withPrintingOfSourceAssignment() { return printSourceAssignment; }
			@Override public boolean withPrintingOfLogicalPlan() { return printLogicalPlan; }
			@Override public boolean withPrintingOfPhysicalPlan() { return printPhysicalPlan; }
		};

		return new HeFQUINEngineConfigReader().read(confDescr, ctx);
	}

}
