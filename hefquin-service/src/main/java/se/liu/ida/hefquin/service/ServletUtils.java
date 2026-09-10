package se.liu.ida.hefquin.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.sparql.resultset.ResultsFormat;

import se.liu.ida.hefquin.engine.QueryProcessingStatsAndExceptions;

/**
 * Utility class for servlet-based integration of the HeFQUIN query engine.
 */
public class ServletUtils
{
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
	 * Converts the exceptions of the given object into a JSON array.
	 * Each exception is represented by its message and, if debug mode
	 * is enabled, the full stack trace for each exception.
	 *
	 * @param statsAndExcs object that contain a list of exceptions;
	 *                     may be null or empty
	 * @param debug        {@code true} if full stack trace for each exception should be
	 *                     included
	 * @return a JSON array where each entry contains the exception message and, if debug
	 *         mode is enabled, its full stack trace
	 */
	public static JsonArray getExceptions( final QueryProcessingStatsAndExceptions statsAndExcs, final boolean debug ) {
		if ( statsAndExcs == null || ! statsAndExcs.containsExceptions() )
			return new JsonArray();
		else
			return getExceptions( statsAndExcs.getExceptions(), debug );
	}

	/**
	 * Converts a list of exceptions into a JSON array. Each exception is represented
	 * by its message and, if debug mode is enabled, its full stack trace.
	 *
	 * @param exceptions the list of exceptions encountered during query processing;
	 *                   may be null or empty
	 * @param debug      {@code true} if full stack trace for each exception should be
	 *                   included
	 * @return a JSON array where each entry contains the exception message and, if debug
	 *         mode is enabled, its full stack trace
	 */
	public static JsonArray getExceptions( final List<Exception> exceptions, final boolean debug ) {
		final JsonArray list = new JsonArray();
		if ( exceptions != null && ! exceptions.isEmpty() ) {
			for ( int i = 0; i < exceptions.size(); i++ ) {
				final JsonObject exception = new JsonObject();
				final Exception ex = exceptions.get(i);
				exception.put( "type", ex.getClass().getName() );
				exception.put( "msg", ex.getMessage() );

				if ( debug ) {
					final StringWriter sw = new StringWriter();
					final PrintWriter pw = new PrintWriter(sw);
					pw.println( "StackTrace:" );
					ex.printStackTrace( pw );
					pw.close();

					exception.put( "stacktrace", sw.toString() );
				}

				list.add( exception );
			}
		}
		return list;
	}
}
