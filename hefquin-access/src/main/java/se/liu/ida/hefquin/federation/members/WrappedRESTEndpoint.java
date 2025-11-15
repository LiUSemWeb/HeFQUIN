package se.liu.ida.hefquin.federation.members;

import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;

/**
 * This interface represents a REST endpoint for which HeFQUIN has a wrapper
 * via which it is possible evaluate SPARQL graph patterns over an RDF view
 * of the data obtained from this endpoint.
 */
public interface WrappedRESTEndpoint extends RESTEndpoint
{
	/**
	 * Assuming the given string is the content of a response retrieved
	 * when issuing a request to this REST endpoint, this method returns
	 * the result of evaluating the given graph pattern over an RDF view
	 * of this content.
	 *
	 * @param pattern - the graph pattern to be evaluated over
	 *                  the RDF view of the given data
	 * @param data - the content of a response retrieved via a
	 *               successful request to this REST endpoint
	 * @return the result of evaluating the pattern over the RDF view,
	 *         in the form of a sequence of solution mappings
	 * @throws DataConversionException if the conversion into RDF fails
	 */
	List<SolutionMapping> evaluatePatternOverRDFView( SPARQLGraphPattern pattern, String data )
			throws DataConversionException;


	public static class DataConversionException extends Exception {
		private static final long serialVersionUID = -296979419386626032L;

		public DataConversionException( final String msg, final Throwable cause ) {
			super(msg, cause);
		}
	}

}
