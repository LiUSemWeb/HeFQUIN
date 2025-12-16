package se.liu.ida.hefquin.federation.members;

import org.apache.jena.datatypes.RDFDatatype;

import se.liu.ida.hefquin.federation.FederationMember;

public interface RESTEndpoint extends FederationMember
{
	/** Returns the URL of this REST endpoint. */
	String getURL();

	/**
	 * Returns the number of query parameters that need to be passed
	 * in requests to this REST endpoint. This number is the size of
	 * the iterable returned by {@link #getParameters()}.
	 *
	 * @return number of query parameters expected by this endpoint
	 */
	int getNumberOfParameters();

	/**
	 * Returns the types of query parameters that need to be passed in
	 * requests to this REST endpoint. The number of query parameters
	 * (i.e., the number of elements in the returned iterable) can be
	 * obtained by {@link #getNumberOfParameters()}.
	 *
	 * @return types of query parameters expected by this endpoint
	 */
	Iterable<Parameter> getParameters();

	/**
	 * Captures an expected query parameter for REST endpoints.
	 */
	public interface Parameter {
		String getName();
		RDFDatatype getType();
	}

}
