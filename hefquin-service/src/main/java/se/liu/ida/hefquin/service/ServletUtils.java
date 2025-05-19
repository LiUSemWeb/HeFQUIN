package se.liu.ida.hefquin.service;

import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.query.ARQ;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.resultset.ResultsFormat;

import se.liu.ida.hefquin.engine.HeFQUINEngine;
import se.liu.ida.hefquin.engine.HeFQUINEngineConfigReader;
import se.liu.ida.hefquin.engine.HeFQUINEngineConfigReader.Context;
import se.liu.ida.hefquin.engine.HeFQUINEngineDefaultComponents;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.federation.catalog.FederationDescriptionReader;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanPrinter;

/**
 * Utility class for servlet-based integration of the HeFQUIN query engine.
 */
public class ServletUtils
{
	/**
	 * Constructs a {@link HeFQUINEngineConfigReader.Context} using the given
	 * federation configuration file. This context includes initialized executor
	 * services and the federation catalog, with default values for optional flags.
	 *
	 * @param fedConfFilename the path to the federation configuration file
	 * @return a configured {@link HeFQUINEngineConfigReader.Context} instance
	 */
	public static Context getCtx( final String fedConfFilename ) {
		final ExecutorService execServiceForFedAccess = HeFQUINEngineDefaultComponents
				.createExecutorServiceForFedAccess();
		final ExecutorService execServiceForPlanTasks = HeFQUINEngineDefaultComponents
				.createExecutorServiceForPlanTasks();
		final FederationCatalog cat = FederationDescriptionReader.readFromFile( fedConfFilename );
		final boolean isExperimentRun = false;
		final boolean skipExecution = false;
		final Context ctx = new HeFQUINEngineConfigReader.Context() {
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

		return ctx;
	}

	/**
	 * Loads the engine configuration model from the specified RDF or Turtle file.
	 *
	 * @param engineConfFilename the path to the engine configuration file
	 * @return a Jena {@link Model} representing the engine configuration
	 */
	public static Model getConfDesc( final String engineConfFilename ) {
		final Model confDescr = RDFDataMgr.loadModel( engineConfFilename );
		return confDescr;
	}

	/**
	 * Instantiates a {@link HeFQUINEngine} using the provided configuration context
	 * and RDF model. This method initializes Apache Jena (via {@link ARQ#init()})
	 * and integrates the HeFQUIN engine into the Jena environment.
	 *
	 * @param ctx       the engine configuration context, including executors and
	 *                  federation metadata
	 * @param confDescr the RDF model containing engine configuration
	 * @return a fully configured and integrated {@link HeFQUINEngine} instance
	 */
	public static HeFQUINEngine getEngine( final HeFQUINEngineConfigReader.Context ctx,
	                                       final Model confDescr ) {
		final HeFQUINEngine engine = new HeFQUINEngineConfigReader().read( confDescr, ctx );
		ARQ.init();
		engine.integrateIntoJena();
		return engine;
	}

	/**
	 * Converts a MIME type string into a corresponding {@link ResultsFormat}
	 * supported by Jena's SPARQL result serialization.
	 *
	 * Recognized MIME types:
	 * - application/sparql-results+json
	 * - application/sparql-results+xml
	 * - text/csv
	 * - text/tab-separated-values
	 *
	 * If the MIME type is unrecognized or null, {@code ResultsFormat.FMT_RS_CSV} is
	 * returned by default.
	 *
	 * @param mimeType the MIME type string from the HTTP Accept header or elsewhere
	 * @return the corresponding {@link ResultsFormat}, or null if the input was
	 *         null
	 */
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
		case "text/tab-separated-values":
			resultsFormat = ResultsFormat.FMT_RS_TSV;
			break;
		default:
			resultsFormat = ResultsFormat.FMT_RS_CSV;
			break;
		}
		return resultsFormat;
	}

	/**
	 * Converts a list of exceptions into a JSON array, including class name, message, and full stack trace for each
	 * exception.
	 *
	 * @param exceptions the list of exceptions encountered during query processing; may be null or empty
	 * @return a JSON array where each entry is a string representation of an exception and its stack trace
	 */
	public static JsonArray getExceptions( final List<Exception> exceptions ) {
		final JsonArray list = new JsonArray();
		if ( exceptions != null && ! exceptions.isEmpty() ) {
			for ( int i = 0; i < exceptions.size(); i++ ) {
				final Throwable rootCause = getRootCause( exceptions.get( i ) );
				final StringWriter sw = new StringWriter();
				sw.append( rootCause.getClass().getName() + ": " + rootCause.getMessage() );
				list.add( sw.toString() );
			}
		}
		return list;
	}

	/**
	 * Returns the root cause of a throwable by traversing the cause chain.
	 *
	 * This method follows the chain of {@code Throwable.getCause()} until it reaches the deepest non-null cause. If the
	 * input {@code throwable} has no cause, the method returns the throwable itself.
	 *
	 * @param throwable the throwable from which to extract the root cause
	 * @return the root cause of the throwable, or {@code null} if {@code throwable} is {@code null}
	 */
	private static Throwable getRootCause( Throwable throwable ) {
		Throwable cause = throwable;
		while ( cause.getCause() != null ) {
			cause = cause.getCause();
		}
		return cause;
	}
}
