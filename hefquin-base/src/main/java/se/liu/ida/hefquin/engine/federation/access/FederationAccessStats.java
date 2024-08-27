package se.liu.ida.hefquin.engine.federation.access;

import se.liu.ida.hefquin.engine.utils.Stats;

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
