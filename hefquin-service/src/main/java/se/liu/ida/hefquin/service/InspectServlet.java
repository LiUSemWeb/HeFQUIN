package se.liu.ida.hefquin.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import se.liu.ida.hefquin.base.utils.StatsPrinter;
import se.liu.ida.hefquin.engine.HeFQUINEngine;
import se.liu.ida.hefquin.engine.IllegalQueryException;
import se.liu.ida.hefquin.engine.QueryProcessingStatsAndExceptions;
import se.liu.ida.hefquin.engine.UnsupportedQueryException;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.TextBasedLogicalPlanPrinterImpl;
import se.liu.ida.hefquin.engine.queryplan.utils.TextBasedPhysicalPlanPrinterImpl;

/**
 * Servlet for handling SPARQL inspect queries via HTTP GET and POST requests.
 */
public class InspectServlet extends HttpServlet
{
	private static Logger logger = LoggerFactory.getLogger( InspectServlet.class );
	private static final long serialVersionUID = 1L;
	private static HeFQUINEngine engine;

	// Request Content-Types
	private static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
	private static final String CONTENT_TYPE_SPARQL_QUERY = "application/sparql-query";

	// Plan printers
	private static final LogicalPlanPrinter logicalPlanPrinter = new TextBasedLogicalPlanPrinterImpl();
	private static final PhysicalPlanPrinter physicalPlanPrinter = new TextBasedPhysicalPlanPrinterImpl();
	private static final LogicalPlanPrinter sourceAssignmentPrinter = new TextBasedLogicalPlanPrinterImpl();

	/**
	 * Initializes the servlet and retrieves the HeFQUIN engine from the servlet
	 * context.
	 *
	 * @param config the servlet configuration
	 * @throws ServletException if the engine initialization fails
	 */
	@Override
	public void init( final ServletConfig config ) throws ServletException {
		super.init( config );
		final Object obj = getServletContext().getAttribute( "engine" );
		if ( obj == null ) {
			throw new ServletException( "HeFQUIN engine not found in servlet context." );
		}
		engine = (HeFQUINEngine) obj;
	}

	/**
	 * Handles HTTP POST requests containing SPARQL queries. Supports both
	 * 'application/sparql-query' and 'application/x-www-form-urlencoded'.
	 *
	 * @param request  the HTTP request
	 * @param response the HTTP response
	 * @throws IOException
	 */
	@Override
	protected void doPost( final HttpServletRequest request,
	                       final HttpServletResponse response )
			throws IOException
	{
		final String contentType = request.getHeader( "Content-Type" );

		final String query;
		if ( CONTENT_TYPE_SPARQL_QUERY.equals( contentType ) ) {
			try ( final BufferedReader reader = request.getReader() ) {
				query = reader.lines().collect( Collectors.joining( "\n" ) );
			}
		}
		else if ( CONTENT_TYPE_FORM_URLENCODED.equals( contentType ) ) {
			query = request.getParameter( "query" );
		}
		else {
			writeJsonError( response, 415, new JsonString ( "Unsupported 'Content-Type' header." ) );
			return;
		}
		executeRequest( query, request, response );
	}

	/**
	 * Handles HTTP GET requests containing a SPARQL query passed via the 'query' parameter.
	 *
	 * @param request  the HTTP request
	 * @param response the HTTP response
	 * @throws IOException
	 */
	@Override
	protected void doGet( final HttpServletRequest request,
	                      final HttpServletResponse response )
		throws IOException
	{
		final String query = request.getParameter( "query" );
		executeRequest( query, request, response );
	}

	/**
	 * Executes the given SPARQL query using the HeFQUIN engine and returns the
	 * inspection results as JSON. Handles query parsing, and error reporting.
	 *
	 * @param query    the SPARQL query string (must not be null or empty)
	 * @param request  the HTTP request containing headers and parameters
	 * @param response the HTTP response used to return the query result or an error message
	 * @throws IOException if an I/O error occurs while writing the response
	 */
	private void executeRequest( final String query,
	                             final HttpServletRequest request,
	                             final HttpServletResponse response )
			throws IOException
	{
		response.setCharacterEncoding( "UTF-8" );

		// Ensure query is not null or empty
		if ( query == null || query.trim().isEmpty() ) {
			writeJsonError( response, 400, new JsonString( "SPARQL query is missing or empty" ) );
			return;
		}

		final String mimeType = "application/sparql-results+json";

		try {
			logger.debug( "Received SPARQL query: {}", query );
			final JsonObject result = execute( query, mimeType );
			response.setStatus( 200 );
			response.setContentType( mimeType );
			response.getWriter().write( result.toString() );

		}
		catch ( final IllegalQueryException e ) {
			writeJsonError( response,
			                400,
			                new JsonString( "The given query is invalid: " + e.getMessage() ) );
			return;
		}
		catch ( final UnsupportedQueryException e ) {
			writeJsonError( response, 501, new JsonString( e.getMessage() ) );
			return;
		}
		catch ( final Exception e ) {
			logger.error( "Query execution failed", e );
			writeJsonError( response,
			                500,
			                new JsonString( "Error during query execution: " + e.getLocalizedMessage() ) );
			return;
		}
	}

	/**
	 * Executes the SPARQL query using the engine and builds the JSON inspection result.
	 *
	 * @param queryString the SPARQL query string
	 * @param mimeType    the MIME type for the response format
	 * @return the inspection result as a JSON object
	 */
	private static JsonObject execute( final String queryString,
	                                   final String mimeType )
			throws UnsupportedQueryException, IllegalQueryException
	{
		final Query query = QueryFactory.create( queryString );
		final ResultsFormat resultsFormat = ServletUtils.convert( mimeType );
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		final JsonObject inspectionResults = new JsonObject();
		try ( PrintStream ps = new PrintStream( baos, true, StandardCharsets.UTF_8 ) ) {
			final QueryProcessingStatsAndExceptions statsAndExceptions =
					engine.executeQueryAndPrintResult( query,
					                                   resultsFormat,
					                                   ps );

			inspectionResults.put( "exceptions",
			                       ServletUtils.getExceptions(statsAndExceptions) );

			if( statsAndExceptions != null ){
				inspectionResults.put( "queryMetrics", StatsPrinter.statsAsJson(statsAndExceptions) );
				inspectionResults.put( "logicalPlan", getLogicalPlan(statsAndExceptions) );
				inspectionResults.put( "physicalPlan", getPhysicalPlan(statsAndExceptions) );
				inspectionResults.put( "sourceAssignment", getSourceAssignment(statsAndExceptions) );
				inspectionResults.put( "federationAccessStats",
				                       StatsPrinter.statsAsJson( engine.getFederationAccessStats() ) );
			}
		}
		return inspectionResults;
	}

	/**
	 * Writes a JSON-formatted error response with the given HTTP status code and
	 * error message. The response body will contain a JSON object of the form:
	 * {@code {"error": "<message>"}}.
	 *
	 * @param response   the HTTP response to write to
	 * @param statusCode the HTTP status code to set (e.g., 400, 415, 500)
	 * @param message    the error message to include in the JSON body
	 * @throws IOException if writing the response fails
	 */
	private static void writeJsonError( final HttpServletResponse response,
	                                    final int statusCode,
	                                    final JsonValue message )
			throws IOException
	{
		response.setStatus( statusCode );
		response.setContentType( "application/json" );
		final JsonObject msg = new JsonObject();
		msg.put( "error", message );
		response.getWriter().write( msg.toString() );
	}

	/**
	 * Generates a textual representation of the logical query plan produced during
	 * query planning.
	 *
	 * @param stats the query processing statistics containing the logical plan
	 * @return a JSON string containing the textual representation of the logical plan
	 */
	private static JsonValue getLogicalPlan( final QueryProcessingStatsAndExceptions stats ) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream( baos );
		logicalPlanPrinter.print( stats.getQueryPlanningStats().getResultingLogicalPlan(), ps );
		return new JsonString( baos.toString() );
	}

	/**
	 * Generates a textual representation of the physical query plan produced during
	 * query planning.
	 *
	 * @param stats the query processing statistics containing the physical plan
	 * @return a JSON string containing the textual representation of the physical plan
	 */
	private static JsonValue getPhysicalPlan( final QueryProcessingStatsAndExceptions stats ) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream( baos );
		physicalPlanPrinter.print( stats.getQueryPlanningStats().getResultingPhysicalPlan(), ps );
		return new JsonString( baos.toString() );
	}

	/**
	 * Generates a textual representation of the logical query plan annotated with
	 * source assignments. Currently uses the same printer as the logical plan
	 * printer; a dedicated printer is not available.
	 *
	 * @param stats the query processing statistics containing the logical plan
	 * @return a JSON string containing the source assignment information
	 */
	private static JsonValue getSourceAssignment( final QueryProcessingStatsAndExceptions stats ) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream( baos );
		sourceAssignmentPrinter.print( stats.getQueryPlanningStats().getResultingLogicalPlan(), ps );
		return new JsonString( baos.toString() );
	}
}
