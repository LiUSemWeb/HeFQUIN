package se.liu.ida.hefquin.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.system.stream.StreamManager;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.HeFQUINEngine;
import se.liu.ida.hefquin.engine.queryproc.QueryProcStats;

@WebServlet
public class HeFQUINServlet extends HttpServlet {
	private static Logger logger = LoggerFactory.getLogger( HeFQUINServlet.class );
	private static final long serialVersionUID = 5902821543508443162L;

	private static HeFQUINEngine engine;
	private static final List<String> SUPPORTED_MIME_TYPES = Arrays.asList(
		"application/sparql-results+json",
		"application/sparql-results+xml",
		"text/csv",
		"text/tsv",
		"text/tab-separated-values"
	);

	@Override
	public void init( ServletConfig config ) throws ServletException {
		super.init( config );

		final String configurationFile = System.getProperty( "hefquin.configuration", "DefaultEngineConf.ttl" );
		final String federationFile = System.getProperty( "hefquin.federation", "DefaultFedConf.ttl" );

		logger.info( "--- Settings ---" );
		logger.info( "hefquin.configuration: " + configurationFile );
		logger.info( "hefquin.federation:    " + federationFile );

		check( configurationFile );
		check( federationFile );

		// Initialize engine
		engine = HeFQUINServerUtils.getEngine( federationFile, configurationFile );
		logger.info( "Engine initilized" );
	}

	public void check( String filenameOrURI ) {
		TypedInputStream in = StreamManager.get().open(filenameOrURI);
		if ( in == null ) {
			throw new RuntimeException( "File not found: " + filenameOrURI );
		}
		in.close();
	}

	@Override
	protected void doPost( final HttpServletRequest request, final HttpServletResponse response )
			throws ServletException, IOException {
		final Iterator<String> acceptHeader = request.getHeaders( "Accept" ).asIterator();
		final String accept = findSupportedMimeType( acceptHeader );
		final String contentType = getOrDefault( request.getHeader( "Content-Type" ), "" );
		response.setCharacterEncoding( "utf-8" );

		String query = null;
		boolean printQueryProcMeasurements = false;
		switch ( contentType ) {
		case "application/sparql-query":
			query = readRequestBody( request );
			break;
		case "application/x-www-form-urlencoded":
			query = request.getParameter( "query" );
			printQueryProcMeasurements = "true".equals(request.getParameter("printQueryProcMeasurements"));
			break;
		default:
			response.setStatus( 415 ); // Unsupported Media Type
			response.getWriter().println( "{\"error\": \"Unsupported content type: " + contentType + " \"}" );
			return;
		}

		// Ensure query is not null or empty
		if ( query == null || query.trim().isEmpty() ) {
			response.setStatus( 400 );
			response.getWriter().println( "{\"error\": \"SPARQL query is missing or empty\"}" );
			return;
		}

		// Check accept header
		if ( accept == null ) {
			response.setStatus( 415 ); // Unsupported Media Type
			response.getWriter().println( "{\"error\": \"Unsupported accept type: " + accept + "\"}" );
			return;
		}

		try {
			final String result = execute( query, accept, printQueryProcMeasurements );
			response.setStatus( 200 );
			response.setHeader( "Content-Type", accept );
			response.getWriter().println( result );
			return;
		} catch ( Exception e ) {
			response.setStatus( 500 );
			response.getWriter().println( "{\"error\": \"" + e.getLocalizedMessage() + "\"}" );
			return;
		}
	}

	@Override
	protected void doGet( HttpServletRequest request, HttpServletResponse response )
			throws ServletException, IOException {
		final Iterator<String> acceptHeader = request.getHeaders( "Accept" ).asIterator();
		final String accept = findSupportedMimeType( acceptHeader );
		response.setCharacterEncoding( "utf-8" );

		final String query = request.getParameter( "query" );

		// Ensure query is not null or empty
		if ( query == null || query.trim().isEmpty() ) {
			response.setStatus( 400 );
			response.getWriter().println( "{\"error\": \"SPARQL query is missing or empty\"}" );
			return;
		}

		// Check accept header
		if ( accept == null ) {
			response.setStatus( 415 ); // Unsupported Media Type
			response.getWriter().println( "{\"error\": \"Unsupported accept type: " + accept + "\"}" );
			response.getWriter().flush();
			return;
		}

		try {
			final String result = execute( query, accept );
			response.setStatus( 200 );
			response.setHeader( "Content-Type", accept );
			response.getWriter().println( result );
		} catch ( Exception e ) {
			response.setStatus( 500 );
			response.getWriter().println( "{\"error\": \"" + e.getLocalizedMessage() + "\"}" );
			return;
		}
	}

	private static String findSupportedMimeType( Iterator<String> acceptHeader ) {
		if ( acceptHeader == null || ! acceptHeader.hasNext() ) {
			return SUPPORTED_MIME_TYPES.get( 0 );
		}
		// Parse the Accept header
		String mimeType;
		while ( acceptHeader.hasNext() ) {
			mimeType = acceptHeader.next().trim().split( ";" )[0];
			if ( SUPPORTED_MIME_TYPES.contains( mimeType ) ) {
				return mimeType;
			}
		}
		return SUPPORTED_MIME_TYPES.get( 0 );
	}

	public String readRequestBody( HttpServletRequest request ) throws IOException {
		try ( final BufferedReader reader = request.getReader() ) {
			final StringBuilder body = new StringBuilder();
			String line;
			while ( (line = reader.readLine()) != null ) {
				body.append( line + "\n" );
			}
			return body.toString();
		}
	}

	private static String execute( final String queryString, final String mimeType )
		throws UnsupportedEncodingException {
			return execute( queryString, mimeType, false );
	}

	private static String execute( final String queryString, final String mimeType, boolean printQueryProcMeasurements )
			throws UnsupportedEncodingException {
		final Query query = QueryFactory.create( queryString );
		final ResultsFormat resultsFormat = HeFQUINServerUtils.convert( mimeType );
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream( baos, true, "utf-8" );
		final Pair<QueryProcStats, List<Exception>> statsAndExceptions = engine.executeQuery( query, resultsFormat, ps );
		ps.close();
		
		// Return statistics
		if ( printQueryProcMeasurements ) {
			if ( statsAndExceptions != null && statsAndExceptions.object1 != null ) {
				final long overallQueryProcessingTime = statsAndExceptions.object1.getOverallQueryProcessingTime();
				final long planningTime = statsAndExceptions.object1.getPlanningTime();
				final long compilationTime = statsAndExceptions.object1.getCompilationTime();
				final long executionTime = statsAndExceptions.object1.getExecutionTime();
				final String queryProcStats = overallQueryProcessingTime + ", " + planningTime + ", " + compilationTime
						+ ", " + executionTime;
				System.out.println( queryProcStats );
				return queryProcStats;
			}
		}

		return baos.toString();
	}

	public static String getOrDefault( final String value, final String defaultValue ) {
		return value != null ? value : defaultValue;
	}
}
