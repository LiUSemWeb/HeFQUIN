package se.liu.ida.hefquin.service;

import java.io.StringWriter;
import java.util.List;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.sparql.resultset.ResultsFormat;

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
	 * Converts a list of exceptions into a JSON array, including class name,
	 * message, and full stack trace for each exception.
	 *
	 * @param exceptions the list of exceptions encountered during query processing;
	 *                   may be null or empty
	 * @return a JSON array where each entry is a string representation of an
	 *         exception and its stack trace
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
	 * This method follows the chain of {@code Throwable.getCause()} until it
	 * reaches the deepest non-null cause. If the input {@code throwable} has no
	 * cause, the method returns the throwable itself.
	 *
	 * @param throwable the throwable from which to extract the root cause
	 * @return the root cause of the throwable, or {@code null} if {@code throwable}
	 *         is {@code null}
	 */
	private static Throwable getRootCause( Throwable throwable ) {
		Throwable cause = throwable;
		while ( cause.getCause() != null ) {
			cause = cause.getCause();
		}
		return cause;
	}
}
