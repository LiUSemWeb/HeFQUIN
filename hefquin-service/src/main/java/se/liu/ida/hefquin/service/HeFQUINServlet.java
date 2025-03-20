package se.liu.ida.hefquin.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.atlas.json.JsonObject;
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
import se.liu.ida.hefquin.engine.HeFQUINEngine;

@WebServlet
public class HeFQUINServlet extends HttpServlet {
	private static Logger logger = LoggerFactory.getLogger( HeFQUINServlet.class );
	private static final long serialVersionUID = 1L;

	private static HeFQUINEngine engine;
	private static final List<String> SUPPORTED_MIME_TYPES = Arrays.asList(
		"application/sparql-results+json",
		"application/sparql-results+xml",
		"text/csv",
		"text/tab-separated-values"
	);

	@Override
	public void init( final ServletConfig config ) throws ServletException {
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

	public void check( final String filenameOrURI ) {
		final TypedInputStream in = StreamManager.get().open( filenameOrURI );
		if ( in == null )
			throw new RuntimeException( "File not found: " + filenameOrURI );
		in.close();
	}

	@Override
	protected void doPost( final HttpServletRequest request, final HttpServletResponse response )
			throws ServletException, IOException {
		final Iterator<String> acceptHeader = request.getHeaders( "Accept" ).asIterator();
		final String accept = findSupportedMimeType( acceptHeader );
		final String contentType = getOrDefault( request.getHeader( "Content-Type" ), "" );
		response.setCharacterEncoding( "utf-8" );

		final String query;
		switch ( contentType ) {
		case "application/sparql-query":
			query = readRequestBody( request );
			break;
		case "application/x-www-form-urlencoded":
			query = request.getParameter( "query" );
			break;
		default:
			response.setStatus( 415 ); // Unsupported Media Type
			final JsonObject msg = new JsonObject();
			msg.put( "error", "Unsupported content type: " + contentType );
			response.getWriter().write( msg.toString() );
			return;
		}

		// Ensure query is not null or empty
		if ( query == null || query.trim().isEmpty() ) {
			response.setStatus( 400 );
			final JsonObject msg = new JsonObject();
			msg.put( "error", "SPARQL query is missing or empty" );
			response.getWriter().write( msg.toString() );
			return;
		}

		// Check accept header
		if ( accept == null ) {
			response.setStatus( 415 ); // Unsupported Media Type
			final JsonObject msg = new JsonObject();
			msg.put( "error", "Unsupported content type: " + accept );
			response.getWriter().write( msg.toString() );
			return;
		}

		try {
			final String result = execute( query, accept );
			response.setStatus( 200 );
			response.setHeader( "Content-Type", accept );
			response.getWriter().write( result );
			return;
		} catch ( Exception e ) {
			response.setStatus( 500 );
			final JsonObject msg = new JsonObject();
			msg.put( "error", e.getLocalizedMessage() );
			response.getWriter().write( msg.toString() );
			return;
		}
	}

	@Override
	protected void doGet( final HttpServletRequest request, final HttpServletResponse response )
			throws ServletException, IOException {
		final Iterator<String> acceptHeader = request.getHeaders( "Accept" ).asIterator();
		final String accept = findSupportedMimeType( acceptHeader );
		response.setCharacterEncoding( "utf-8" );

		final String query = request.getParameter( "query" );

		// Ensure query is not null or empty
		if ( query == null || query.trim().isEmpty() ) {
			response.setStatus( 400 );
			final JsonObject msg = new JsonObject();
			msg.put( "error", "SPARQL query is missing or empty" );
			response.getWriter().write( msg.toString() );
			return;
		}

		// Check accept header
		if ( accept == null ) {
			response.setStatus( 415 ); // Unsupported Media Type
			final JsonObject msg = new JsonObject();
			msg.put( "error", "Unsupported accept type: " + accept );
			response.getWriter().write( msg.toString() );
			return;
		}

		try {
			final String result = execute( query, accept );
			response.setStatus( 200 );
			response.setHeader( "Content-Type", accept );

			response.getWriter().write( result );
		} catch ( Exception e ) {
			response.setStatus( 500 );
			final JsonObject msg = new JsonObject();
			msg.put( "error", e.getLocalizedMessage() );
			response.getWriter().write( msg.toString() );
			return;
		}
	}

	private static String findSupportedMimeType( final Iterator<String> acceptHeader ) {
		// Parse the accept header
		while ( acceptHeader != null && acceptHeader.hasNext() ) {
			final String mimeType = acceptHeader.next().trim().split( ";" )[0];
			if ( SUPPORTED_MIME_TYPES.contains( mimeType ) )
				return mimeType;
		}
		return SUPPORTED_MIME_TYPES.get( 0 ); // default
	}

	public String readRequestBody( final HttpServletRequest request ) throws IOException {
		try ( final BufferedReader reader = request.getReader() ) {
			return reader.lines().collect( Collectors.joining( "\n" ) );
		}
	}

	private static String execute( final String queryString, final String mimeType )
			throws UnsupportedEncodingException {
		final Query query = QueryFactory.create( queryString );
		final ResultsFormat resultsFormat = HeFQUINServerUtils.convert( mimeType );
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream( baos, true, "utf-8" );
		engine.executeQuery( query, resultsFormat, ps );
		ps.close();
		return baos.toString();
	}

	public static String getOrDefault( final String value, final String defaultValue ) {
		return value != null ? value : defaultValue;
	}
}
