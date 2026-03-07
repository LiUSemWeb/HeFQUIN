package se.liu.ida.hefquin.federation.access;

import java.net.URI;

import se.liu.ida.hefquin.base.query.ExpectedVariables;

/**
 * Represents a request to a particular endpoint of a REST API.
 */
public interface RESTRequest extends DataRetrievalRequest
{
	/**
	 * Returns the URI to be requested.
	 */
	URI getURI();

	@Override
	default ExpectedVariables getExpectedVariables() {
		throw new UnsupportedOperationException();
	}

}
