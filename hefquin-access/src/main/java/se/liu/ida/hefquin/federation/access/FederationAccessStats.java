package se.liu.ida.hefquin.federation.access;

import se.liu.ida.hefquin.base.utils.Stats;

public interface FederationAccessStats extends Stats
{
	long getOverallNumberOfRequestsIssued();
	long getOverallNumberOfRequestsCompleted();

	long getNumberOfSPARQLRequestsIssued();
	long getNumberOfTPFRequestsIssued();
	long getNumberOfBRTPFRequestsIssued();
	long getNumberOfNeo4jRequestsIssued();

	long getNumberOfSPARQLRequestsCompleted();
	long getNumberOfTPFRequestsCompleted();
	long getNumberOfBRTPFRequestsCompleted();
	long getNumberOfNeo4jRequestsCompleted();
}
