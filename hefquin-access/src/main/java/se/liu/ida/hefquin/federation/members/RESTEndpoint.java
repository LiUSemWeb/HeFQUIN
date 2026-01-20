package se.liu.ida.hefquin.federation.members;

import org.apache.jena.datatypes.RDFDatatype;

import se.liu.ida.hefquin.federation.FederationMember;

public interface RESTEndpoint extends FederationMember
{
	/** Returns the URL template of this REST endpoint. */
	String getURLTemplate();

	/**
	 * Returns the number of query parameters that can be passed
	 * in requests to this REST endpoint. This number is the size of
	 * the iterable returned by {@link #getParameters()}.
	 *
	 * @return number of query parameters accepted by this endpoint
	 */
	int getNumberOfParameters();

	/**
	 * Returns the types of query parameters that can be passed in
	 * requests to this REST endpoint. The number of query parameters
	 * (i.e., the number of elements in the returned iterable) can be
	 * obtained by {@link #getNumberOfParameters()}.
	 *
	 * @return types of query parameters accepted by this endpoint
	 */
	Iterable<Parameter> getParameters();

	/**
	 * Returns the parameter with the given name.
	 *
	 * @param name the name of the parameter to be returned
	 * @return the parameter with the given name, or {@code null} if no such
	 *         parameter exists
	 */
	Parameter getParameterByName(String name);

	/**
	 * Captures an expected query parameter for REST endpoints.
	 */
	public interface Parameter {
		String getName();
		RDFDatatype getType();
		Boolean isRequired();
	}

}
