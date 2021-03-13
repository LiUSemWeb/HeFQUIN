package se.liu.ida.hefquin.federation.access;

import java.util.Date;

import se.liu.ida.hefquin.federation.FederationMember;

public interface DataRetrievalResponse
{
	/**
	 * Returns the federation member where this response comes from.
	 */
	FederationMember getFederationMember();

	/**
	 * Returns the time at which this response was retrieved.
	 */
	Date getRetrievalTime();
}
