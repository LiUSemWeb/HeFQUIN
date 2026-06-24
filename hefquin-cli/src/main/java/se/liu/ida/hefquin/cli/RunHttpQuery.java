package se.liu.ida.hefquin.cli;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Pattern;

import org.apache.commons.io.output.NullPrintStream;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.TerminationException;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryVisitor;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.lang.SPARQLParser;
import org.apache.jena.sparql.lang.SPARQLParserFactory;
import org.apache.jena.sparql.lang.SPARQLParserRegistry;
import org.apache.jena.sparql.serializer.QuerySerializerFactory;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.serializer.SerializerRegistry;
import org.apache.jena.sparql.util.QueryExecUtils;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModResultsOut;
import arq.cmdline.ModTime;
import se.liu.ida.hefquin.base.net.http.HttpConstants;
import se.liu.ida.hefquin.cli.modules.ModPlanPrinting;
import se.liu.ida.hefquin.cli.modules.ModQuery;
import se.liu.ida.hefquin.cli.modules.ModResultsOutExt;
import se.liu.ida.hefquin.jenaext.query.SyntaxForHeFQUIN;
import se.liu.ida.hefquin.jenaext.sparql.lang.sparql_12_hefquin.ParserSPARQL12HeFQUIN;

/**
 * A command-line tool that interacts with a HeFQUIN service to execute a SPARQL
 * query over the federation for which the service is set up, and prints the query result
 * returned by the service.
 *
 * The tool sends a SPARQL SELECT query to a given HTTP endpoint using a
 * standard HTTP POST request with SPARQL Protocol encoding. It expects the
 * endpoint to return results in SPARQL JSON Results format, which is then
 * parsed into a Jena {@code ResultSet}.
 *
 * The resulting solution mappings are printed to standard output or written
 * to a file in a user-selected results format.
 */
public class RunHttpQuery extends CmdARQ
{
	protected final ModTime          modTime =          new ModTime();
	protected final ModPlanPrinting  modPlanPrinting =  new ModPlanPrinting();
	protected final ModResultsOut    modResults =       new ModResultsOut();
	protected final ModQuery         modQuery =         new ModQuery();
	protected final ModResultsOutExt modResultsExt =    new ModResultsOutExt();

	protected final ArgDecl argServerAddress = new ArgDecl( ArgDecl.HasValue, "server" );
	protected final ArgDecl argOutputToFile =  new ArgDecl( ArgDecl.HasValue, "outputToFile" );

	protected static final Pattern BASE_PATTERN = Pattern.compile( "\\bBASE\\b\\s*<[^>]+>", Pattern.CASE_INSENSITIVE );

	public static void main( final String[] argv ) {
		new RunHttpQuery( argv ).mainRun();
	}

	public RunHttpQuery( final String[] argv ) {
		super( argv );

		registerHeFQUINJenaIntegration();

		addModule( modTime );
		addModule( modPlanPrinting );
		addModule( modResults );
		addModule( modResultsExt );

		add( argServerAddress, "--server", "Address of HeFQUIN service" );
		add( argOutputToFile, "--outputToFile", "Output file (optional, printing to stdout if omitted)" );

		addModule( modQuery );
	}

	/**
	 * Returns the usage summary string of the command, showing the required arguments.
	 *
	 * @return A string that describes the usage of the command.
	 */
	@Override
	protected String getSummary() {
		return getCommandName() + " " +
		"--query=<query> " +
		"--server=<server address> " +
		"[--outputToFile=<file name>]";
	}

	/**
	 * Returns the command name used to invoke the tool.
	 *
	 * @return The name of the command.
	 */
	@Override
	protected String getCommandName() {
		return "hefquin-client";
	}

	/**
	 * Executes the HTTP SPARQL query command.
	 * This method parses command-line arguments, prepares the output stream
	 * (either standard output or a file if specified) and delegates query
	 * execution to the HTTP request handler.
	 *
	 * It ensures that any file-based output streams are properly closed after
	 * execution, even if an error occurs during query processing.
	 */
	@Override
	protected void exec() {
		if ( ! contains(argServerAddress) )
			cmdError("Must give a server address to send query to", true );

		final PrintStream out;
		final PrintStream ownedStream;
		// Result printout suppression has highest precedence
		if ( modResultsExt.isSuppressResultPrintout() ) {
			out = NullPrintStream.INSTANCE;
			ownedStream = null;
		}
		else if ( contains(argOutputToFile) ) {
			try {
				// Appends to file rather than overwriting
				ownedStream = new PrintStream(
					new FileOutputStream( getValue( argOutputToFile ), true ),
					true );
				out = ownedStream;
			} catch ( final FileNotFoundException e ) {
				cmdError( "Failed to create print stream for output destination: " + getValue( argOutputToFile ), false );
				return;
			}
		}
		else {
			out = System.out;
			ownedStream = null;
		}

		final String serverURI = getValue( argServerAddress );

		final Query query = getQuery();

		try {
			exec( serverURI, query, out );
		}
		finally {
			if (ownedStream != null)
				ownedStream.close();
		}
	}

	/**
	 * Executes a SPARQL query against a remote SPARQL endpoint using HTTP.
	 * The query is sent using the SPARQL Protocol over HTTP and the results
	 * are expected to be returned in SPARQL JSON Results format.
	 *
	 * This method handles HTTP request creation, response validation, error
	 * handling and conversion of the result set into a printable format.
	 * Successful results are written to the provided output stream.
	 */
	protected void exec( final String serverURI,
	                     final Query query,
	                     final PrintStream out ) {
		modTime.startTimer();

		final HttpClient client = HttpClient.newBuilder()
			.connectTimeout( Duration.ofMillis(5000) )
			.build();

		final HttpRequest.Builder builder = HttpRequest.newBuilder()
			.uri( URI.create(serverURI) )
			.header( "Content-Type", "application/sparql-query" )
			.header( "Accept", "application/sparql-results+json" )
			.timeout( Duration.ofSeconds(60) )
			.POST( HttpRequest.BodyPublishers.ofString(prepareQuery( query )) );

		if ( modResultsExt.isSkipExecution() )
			builder.header( HttpConstants.X_HEADER_SKIP_EXECUTION, "true" );

		if ( modPlanPrinting.getSourceAssignmentPrinter() != null )
			builder.header( HttpConstants.X_HEADER_PRINT_SOURCE_ASSIGNMENT, "true" );

		if ( modPlanPrinting.getLogicalPlanPrinter() != null )
			builder.header( HttpConstants.X_HEADER_PRINT_LOGICAL_PLAN, "true" );

		if ( modPlanPrinting.getPhysicalPlanPrinter() != null )
			builder.header( HttpConstants.X_HEADER_PRINT_PHYSICAL_PLAN, "true" );

		if ( modPlanPrinting.getExecutablePlanPrinter() != null )
			builder.header( HttpConstants.X_HEADER_PRINT_EXECUTABLE_PLAN, "true" );

		if ( modResultsExt.needsQueryProcStats() )
			builder.header( HttpConstants.X_HEADER_RETURN_QUERY_PROC_STATS, "true" );

		if ( modResultsExt.needsFedAccessStats() )
			builder.header( HttpConstants.X_HEADER_RETURN_FED_ACCESS_STATS, "true" );

		final HttpRequest request = builder.build();

		final HttpResponse<InputStream> response;
		try {
			response = client.send( request, HttpResponse.BodyHandlers.ofInputStream() );
		}
		catch ( final IOException e ) {
			cmdError( "Request to address at <" + serverURI + "> failed: " + e.getMessage(), true );
			return;
		}
		catch ( final InterruptedException e ) {
			cmdError( "Request to address at <" + serverURI + "> failed: " + e.getMessage(), true );
			return;
		}

		if ( response.statusCode() != 200 ) {
			cmdError( "Request failed with HTTP status " + response.statusCode(), true );
			return;
		}

		final JsonObject obj = JSON.parse(response.body());

		printPlans( obj );

		final ResultSet rs = ResultSetFactory.fromJSON(
			new ByteArrayInputStream(
				obj.getString(HttpConstants.JSON_RESULT)
				.getBytes(StandardCharsets.UTF_8)
			)
		);

		QueryExecUtils.outputResultSet( rs, query.getPrologue(), modResults.getResultsFormat(), out );

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.err.println( "Time: " + modTime.timeStr( time ) + " sec" );
		}

		final JsonValue queryProcStatsValue = obj.get(HttpConstants.JSON_QUERY_PROC_STATS);
		if ( queryProcStatsValue != null ) {
			if ( modResultsExt.isPrintQueryProcStats() ) {
				System.err.println( queryProcStatsValue.toString() );
				System.err.println();
			}
			final String queryProcStatsFile = modResultsExt.getQueryProcStatsFile();
			if ( queryProcStatsFile != null ) {
				ModResultsOutExt.writeContentToFile(
					queryProcStatsFile,
					ps -> ps.print( queryProcStatsValue.toString() ),
					msg -> cmdError( msg, false )
				);
			}
			if ( modResultsExt.isPrintOnelineTimeStats() ) {
				final String queryProcStats = extractOnelineTimeStats( queryProcStatsValue );
				System.out.println( queryProcStats );
			}
			final String oneLineTimeStatsFile = modResultsExt.getOnelineTimeStatsFile();
			if ( oneLineTimeStatsFile != null ) {
				ModResultsOutExt.writeContentToFile(
					oneLineTimeStatsFile,
					ps -> ps.print( extractOnelineTimeStats( queryProcStatsValue ) ),
					msg -> cmdError( msg, false )
				);
			}
		}

		final JsonValue fedAccessStatsValue = obj.get(HttpConstants.JSON_FED_ACCESS_STATS);
		if ( fedAccessStatsValue != null ) {
			if ( modResultsExt.isPrintFedAccessStats() ) {
				System.err.println( fedAccessStatsValue.toString() );
				System.err.println();
			}
			final String fedAccessStatsFile = modResultsExt.getFedAccessStatsFile();
			if ( fedAccessStatsFile != null ) {
				ModResultsOutExt.writeContentToFile(
					fedAccessStatsFile,
					ps -> ps.print( fedAccessStatsValue.toString() ),
					msg -> cmdError( msg, false )
				);
			}
		}
	}

	/**
	 * Registers HeFQUIN-specific integrations with the Jena framework.
	 * This method ensures that queries written in the HeFQUIN syntax are correctly parsed
	 * into Jena's internal query representation and that query serialization
	 * correctly falls back to standard SPARQL 1.2 behaviour when needed.
	 */
	protected void registerHeFQUINJenaIntegration() {
		ARQ.init();
		final SPARQLParserFactory pFact = new SPARQLParserFactory() {
			@Override
			public boolean accept( final Syntax syntax ) {
				return SyntaxForHeFQUIN.syntaxSPARQL_12_HeFQUIN.equals(syntax);
			}

			@Override
			public SPARQLParser create( final Syntax syntax ) {
				return new ParserSPARQL12HeFQUIN();
			}
		};

		SPARQLParserRegistry.addFactory( SyntaxForHeFQUIN.syntaxSPARQL_12_HeFQUIN,
		                                 pFact );

		final QuerySerializerFactory baseFactory =
				SerializerRegistry.get()
						.getQuerySerializerFactory(Syntax.syntaxSPARQL_12);

		final QuerySerializerFactory customFactory = new QuerySerializerFactory() {
			@Override
			public boolean accept(Syntax syntax) {
				return SyntaxForHeFQUIN.syntaxSPARQL_12_HeFQUIN.equals(syntax);
			}

			@Override
			public QueryVisitor create(Syntax syntax, Prologue prologue, IndentedWriter writer) {
				return baseFactory.create(Syntax.syntaxSPARQL_12, prologue, writer);
			}

			@Override
			public QueryVisitor create(Syntax syntax, SerializationContext context, IndentedWriter writer) {
				return baseFactory.create(Syntax.syntaxSPARQL_12, context, writer);
			}
		};

		SerializerRegistry.get().addQuerySerializer(
				SyntaxForHeFQUIN.syntaxSPARQL_12_HeFQUIN,
				customFactory
		);
	}

	/**
	 * Returns the SPARQL query to be executed.
	 *
	 * @return the {@code Query} object
	 * @throws TerminationException if the query file could not be found
	 */
	protected Query getQuery() {
		try {
			return modQuery.getQuery();
		} catch ( final NotFoundException ex ) {
			System.err.println( "Failed to load query: " + ex.getMessage() );
			throw new TerminationException( 1 );
		}
	}

	/**
	 * Prepares a SPARQL query for HTTP transmission by ensuring that any base URI
	 * defined in the query prologue is explicitly included in the serialized query.
	 *
	 * <p>If the query already contains an explicit {@code BASE <...>} declaration,
	 * it is left unchanged. Otherwise, if a base URI is present in the query
	 * prologue (e.g., provided via the {@code --base} argument), it is injected as
	 * a leading {@code BASE} clause.</p>
	 */
	protected static String prepareQuery( final Query query ) {
		final String serialized = query.toString();

		if ( query.getPrologue().getBaseURI() == null )
			return serialized;

		if ( BASE_PATTERN.matcher(serialized).find() )
			return serialized;

		return "BASE <" + query.getPrologue().getBaseURI() + ">\n" + serialized;
	}

	/**
	 * Prints any query planning and execution artifacts contained in the
	 * given response object using the printers configured in the plan
	 * printing module.
	 *
	 * @param obj JSON response object returned by the HeFQUIN service
	 */
	protected void printPlans( final JsonObject obj ) {
		final JsonValue srcasgValue = obj.get(HttpConstants.JSON_SOURCE_ASSIGNMENT);
		if ( srcasgValue != null && srcasgValue.isString() )
			modPlanPrinting.printSourceAssignment( srcasgValue.getAsString().value() );

		final JsonValue lplanValue = obj.get(HttpConstants.JSON_LOGICAL_PLAN);
		if ( lplanValue != null && lplanValue.isString() )
			modPlanPrinting.printLogicalPlan( lplanValue.getAsString().value() );

		final JsonValue pplanValue = obj.get(HttpConstants.JSON_PHYSICAL_PLAN);
		if ( pplanValue != null && pplanValue.isString() )
			modPlanPrinting.printPhysicalPlan( pplanValue.getAsString().value() );

		final JsonValue eplanValue = obj.get(HttpConstants.JSON_EXECUTABLE_PLAN);
		if ( eplanValue != null && eplanValue.isString() )
			modPlanPrinting.printExecutablePlan( eplanValue.getAsString().value() );
	}

	/**
	 * Extracts and formats query processing statistics from the given
	 * {@code JsonValue} object into a comma-separated string.
	 *
	 * The returned string contains the overall query processing time, planning time,
	 * compilation time, and execution time, in that order.
	 *
	 * @param statsAndExceptions the object containing query processing statistics
	 * @return a comma-separated string of query processing statistics
	 */
	private static String extractOnelineTimeStats( final JsonValue statsAndExceptions ) {
		final JsonObject obj = statsAndExceptions.getAsObject();

		final long overallQueryProcessingTime = obj.get("overallQueryProcessingTime").getAsNumber().value().longValue();
		final long planningTime = obj.get("planningTime").getAsNumber().value().longValue();
		final long compilationTime = obj.get("compilationTime").getAsNumber().value().longValue();
		final long executionTime = obj.get("executionTime").getAsNumber().value().longValue();

		final String queryProcStats = overallQueryProcessingTime + ", " + planningTime + ", " + compilationTime
				+ ", " + executionTime;
		return queryProcStats;
	}
}
