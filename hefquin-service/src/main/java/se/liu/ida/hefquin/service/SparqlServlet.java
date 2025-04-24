package se.liu.ida.hefquin.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
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
import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.HeFQUINEngine;
import se.liu.ida.hefquin.engine.IllegalQueryException;
import se.liu.ida.hefquin.engine.UnsupportedQueryException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcStats;

/**
 * Servlet for handling SPARQL queries via HTTP GET and POST requests.
 * Supports multiple result formats and integrates with the HeFQUIN engine.
 */
public class SparqlServlet extends HttpServlet {
	private static Logger logger = LoggerFactory.getLogger( SparqlServlet.class );
	private static final long serialVersionUID = 1L;
	private static HeFQUINEngine engine;

	// Request Content-Types
	private static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
	private static final String CONTENT_TYPE_SPARQL_QUERY    = "application/sparql-query";

	// Accept headers (Response formats for SELECT/ASK results)
	private static final String ACCEPT_SPARQL_RESULTS_JSON   = "application/sparql-results+json";
	private static final String ACCEPT_SPARQL_RESULTS_XML    = "application/sparql-results+xml";
	private static final String ACCEPT_CSV                   = "text/csv";
	private static final String ACCEPT_TSV                   = "text/tab-separated-values";
	private static final String DEFAULT_MIME_TYPE            = ACCEPT_SPARQL_RESULTS_JSON;
	private static final String ACCEPT_WILDCARD              = "*/*";

	private static final Set<String> SUPPORTED_MIME_TYPES = Set.of(
		ACCEPT_SPARQL_RESULTS_JSON,
		ACCEPT_SPARQL_RESULTS_XML,
		ACCEPT_CSV,
		ACCEPT_TSV
	);

	/**
	 * Initializes the servlet.
	 *
	 * @param config the servlet configuration
	 * @throws ServletException if the engine initialization fails
	 */
	@Override
	public void init( final ServletConfig config ) throws ServletException {
		super.init( config );
		engine = (HeFQUINEngine) getServletContext().getAttribute( "engine" );
	}

	/**
	 * Handles HTTP POST requests containing SPARQL queries. Supports both 'application/sparql-query' and
	 * 'application/x-www-form-urlencoded'.
	 *
	 * @param request  the HTTP request
	 * @param response the HTTP response
	 * @throws IOException
	 */
	@Override
	protected void doPost( final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
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
			writeJsonError( response, 415, new JsonString(  "Unsupported 'Content-Type' header." ) );
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
	protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
		final String query = request.getParameter( "query" );
		executeRequest( query, request, response );
	}

	/**
	 * Executes the given SPARQL query using the HeFQUIN engine and writes the result to the HTTP response. Handles
	 * query parsing, content negotiation via the Accept header, and error reporting.
	 *
	 * @param query    the SPARQL query string (must not be null or empty)
	 * @param request  the HTTP request containing headers and parameters
	 * @param response the HTTP response used to return the query result or an error message
	 * @throws IOException if an I/O error occurs while writing the response
	 */
	private void executeRequest( final String query,
	                             final HttpServletRequest request,
	                             final HttpServletResponse response ) throws IOException {
		response.setCharacterEncoding( "utf-8" );

		// Ensure query is not null or empty
		if ( query == null || query.trim().isEmpty() ) {
			writeJsonError( response, 400, new JsonString( "SPARQL query is missing or empty" ) );
			return;
		}

		// Check accept header
		final String mimeType = findSupportedMimeType( request.getHeaders( "Accept" ) );
		if ( mimeType == null ) {
			writeJsonError( response, 406, new JsonString( "Unsupported 'Accept' header" ) );
			return;
		}

		try {
			logger.debug( "Received SPARQL query: {}", query );

			final JsonObject result = execute( query, mimeType );
			if ( ! result.get( "exceptions" ).getAsArray().isEmpty() ) {
				writeJsonError( response, 500, result.get( "exceptions" ) );
				return;
			}

			response.setStatus( 200 );
			response.setContentType( mimeType );
			response.getWriter().write( result.getString( "result" ) );
		}
		catch ( final IllegalQueryException e ) {
			writeJsonError( response, 400, new JsonString( "The given query is invalid: " + e.getMessage() ) );
			return;
		}
		catch ( final UnsupportedQueryException e ) {
			writeJsonError( response, 501, new JsonString( e.getMessage() ) );
			return;
		}
		catch ( final Exception e ) {
			logger.error( "Query execution failed", e );
			writeJsonError( response, 500, new JsonString( "Error during query execution: " + e.getLocalizedMessage() ) );
			return;
		}
	}

	/**
	 * Finds the first supported MIME type from the provided Accept header.
	 *
	 * @param acceptHeaders iterator over Accept header values
	 * @return a supported MIME type, or a default if accept header is null or all content types are supported
	 */
	private static String findSupportedMimeType( final Enumeration<String> acceptHeaders ) {
		if ( acceptHeaders == null || ! acceptHeaders.hasMoreElements() ) {
			return ACCEPT_SPARQL_RESULTS_JSON;
		}

		while ( acceptHeaders.hasMoreElements() ) {
			final String headerLine = acceptHeaders.nextElement();
			for ( final String value : headerLine.split( "," ) ) {
				final String mediaType = value.split( ";" )[0].trim();
				if ( SUPPORTED_MIME_TYPES.contains( mediaType ) ) {
					return mediaType;
				}

				if ( ACCEPT_WILDCARD.equals( mediaType ) ) {
					return DEFAULT_MIME_TYPE;
				}
			}
		}

		return null;
	}

	/**
	 * Executes the given SPARQL query using the HeFQUIN engine and returns the serialized result.
	 *
	 * @param queryString the SPARQL query string
	 * @param mimeType    the MIME type for the response format
	 * @return the query result and exceptions in JSON format
	 */
	private static JsonObject execute( final String queryString, final String mimeType )
			throws UnsupportedQueryException, IllegalQueryException
	{
		final Query query = QueryFactory.create( queryString );
		final ResultsFormat resultsFormat = ServletUtils.convert( mimeType );
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		final Pair<QueryProcStats, List<Exception>> statsAndExceptions;
		try ( PrintStream ps = new PrintStream( baos, true, StandardCharsets.UTF_8 ) ) {
			statsAndExceptions = engine.executeQuery( query, resultsFormat, ps );
		}

		final JsonObject res = new JsonObject();
		res.put( "result", baos.toString() );
		res.put( "exceptions", ServletUtils.getExceptions( statsAndExceptions.object2 ) );
		return res;
	}

	/**
	 * Writes a JSON-formatted error response with the given HTTP status code and error message. The response body will
	 * contain a JSON object of the form: {@code {"error": "<message>"}}.
	 *
	 * @param response   the HTTP response to write to
	 * @param statusCode the HTTP status code to set (e.g., 400, 415, 500)
	 * @param message    the error message to include in the JSON body
	 * @throws IOException if writing the response fails
	 */
	private static void writeJsonError( final HttpServletResponse response, final int statusCode, final JsonValue message )
			throws IOException {
		response.setStatus( statusCode );
		response.setContentType( "application/json" );
		final JsonObject msg = new JsonObject();
		msg.put( "error", message );
		response.getWriter().write( msg.toString() );
	}
}
