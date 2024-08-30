package se.liu.ida.hefquin.engine.federation.access;

import java.util.Date;

import se.liu.ida.hefquin.engine.federation.FederationMember;

public interface DataRetrievalResponse
{
	/**
	 * Returns the federation member where this response comes from.
	 */
	FederationMember getFederationMember();

	/**
	 * Returns the request that has been issued to obtain this response.
	 */
	DataRetrievalRequest getRequest();

	/**
	 * Returns the time at which the corresponding data retrieval
	 * request (see {@link #getRequest()}) was started.
	 */
	Date getRequestStartTime();

	/**
	 * Returns the time at which the retrieval of this response was completed.
	 */
	Date getRetrievalEndTime();
}
