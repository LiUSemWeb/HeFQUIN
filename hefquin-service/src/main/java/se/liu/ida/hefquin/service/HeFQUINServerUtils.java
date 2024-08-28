package se.liu.ida.hefquin.service;

import java.util.concurrent.ExecutorService;

import org.apache.jena.query.ARQ;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.resultset.ResultsFormat;

import se.liu.ida.hefquin.engine.HeFQUINEngine;
import se.liu.ida.hefquin.engine.HeFQUINEngineConfigReader;
import se.liu.ida.hefquin.engine.HeFQUINEngineDefaultComponents;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.federation.catalog.FederationDescriptionReader;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanPrinter;

public class HeFQUINServerUtils {

	public static HeFQUINEngine getEngine( final String fedConfFilename, final String engineConfFilename ) {
		// Make sure ARQ is initiated
		ARQ.init();
		final ExecutorService execServiceForFedAccess = HeFQUINEngineDefaultComponents
				.createExecutorServiceForFedAccess();
		final ExecutorService execServiceForPlanTasks = HeFQUINEngineDefaultComponents
				.createExecutorServiceForPlanTasks();
		final FederationCatalog cat = FederationDescriptionReader.readFromFile( fedConfFilename );
		final boolean isExperimentRun = false;
		final boolean skipExecution = false;

		final HeFQUINEngineConfigReader.Context ctx = new HeFQUINEngineConfigReader.Context() {
			@Override
			public ExecutorService getExecutorServiceForFederationAccess() {
				return execServiceForFedAccess;
			}

			@Override
			public ExecutorService getExecutorServiceForPlanTasks() {
				return execServiceForPlanTasks;
			}

			@Override
			public FederationCatalog getFederationCatalog() {
				return cat;
			}

			@Override
			public boolean isExperimentRun() {
				return isExperimentRun;
			}

			@Override
			public boolean skipExecution() {
				return skipExecution;
			}

			@Override
			public LogicalPlanPrinter getSourceAssignmentPrinter() {
				return null;
			}

			@Override
			public LogicalPlanPrinter getLogicalPlanPrinter() {
				return null;
			}

			@Override
			public PhysicalPlanPrinter getPhysicalPlanPrinter() {
				return null;
			}
		};

		final Model confDescr = RDFDataMgr.loadModel( engineConfFilename );
		final HeFQUINEngine engine = new HeFQUINEngineConfigReader().read( confDescr, ctx );
		engine.integrateIntoJena();
		return engine;
	}

	public static ResultsFormat convert( final String mimeType ) {
		if ( mimeType == null )
			return null;

		ResultsFormat resultsFormat;
		switch ( mimeType ) {
		case "application/sparql-results+json":
			resultsFormat = ResultsFormat.FMT_RS_JSON;
			break;
		case "application/sparql-results+xml":
			resultsFormat = ResultsFormat.FMT_RS_XML;
			break;
		case "text/csv":
			resultsFormat = ResultsFormat.FMT_RS_CSV;
			break;
		case "text/tsv":
			resultsFormat = ResultsFormat.FMT_RS_TSV;
			break;
		case "text/tab-separated-values":
			resultsFormat = ResultsFormat.FMT_RS_TSV;
			break;
		default:
			resultsFormat = ResultsFormat.FMT_RS_CSV;
			break;
		}
		return resultsFormat;
	}
}
