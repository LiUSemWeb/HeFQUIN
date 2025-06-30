package se.liu.ida.hefquin.federation.access.impl;

import se.liu.ida.hefquin.base.utils.StatsImpl;
import se.liu.ida.hefquin.federation.access.FederationAccessStats;

public class FederationAccessStatsImpl extends StatsImpl implements FederationAccessStats
{
	protected static final String enNumberOfSPARQLRequestsIssued   = "numberOfSPARQLRequestsIssued";
	protected static final String enNumberOfTPFRequestsIssued      = "numberOfTPFRequestsIssued";
	protected static final String enNumberOfBRTPFRequestsIssued    = "numberOfBRTPFRequestsIssued";
	protected static final String enNumberOfNeo4jRequestsIssued    = "numberOfNeo4jRequestsIssued";

	protected static final String enNumberOfSPARQLRequestsCompleted   = "numberOfSPARQLRequestsCompleted";
	protected static final String enNumberOfTPFRequestsCompleted      = "numberOfTPFRequestsCompleted";
	protected static final String enNumberOfBRTPFRequestsCompleted    = "numberOfBRTPFRequestsCompleted";
	protected static final String enNumberOfNeo4jRequestsCompleted    = "numberOfNeo4jRequestsCompleted";

	protected static final String enOverallNumberOfRequestsIssued    = "overallNumberOfRequestsIssued";
	protected static final String enOverallNumberOfRequestsCompleted  = "overallNumberOfRequestsCompleted";

	public FederationAccessStatsImpl( final long numberOfSPARQLRequestsIssued,
	                                  final long numberOfTPFRequestsIssued,
	                                  final long numberOfBRTPFRequestsIssued,
	                                  final long numberOfNeo4jRequestsIssued,
	                                  final long numberOfSPARQLRequestsCompleted,
	                                  final long numberOfTPFRequestsCompleted,
	                                  final long numberOfBRTPFRequestsCompleted,
	                                  final long numberOfNeo4jRequestsCompleted ) {
		put( enNumberOfSPARQLRequestsIssued,  Long.valueOf(numberOfSPARQLRequestsIssued) );
		put( enNumberOfTPFRequestsIssued,     Long.valueOf(numberOfTPFRequestsIssued) );
		put( enNumberOfBRTPFRequestsIssued,   Long.valueOf(numberOfBRTPFRequestsIssued) );
		put( enNumberOfNeo4jRequestsIssued,   Long.valueOf(numberOfNeo4jRequestsIssued) );

		put( enNumberOfSPARQLRequestsCompleted,  Long.valueOf(numberOfSPARQLRequestsCompleted) );
		put( enNumberOfTPFRequestsCompleted,     Long.valueOf(numberOfTPFRequestsCompleted) );
		put( enNumberOfBRTPFRequestsCompleted,   Long.valueOf(numberOfBRTPFRequestsCompleted) );
		put( enNumberOfNeo4jRequestsCompleted,   Long.valueOf(numberOfNeo4jRequestsCompleted) );

		final long overallNumberOfRequestsIssued = numberOfSPARQLRequestsIssued
		                                         + numberOfTPFRequestsIssued
		                                         + numberOfBRTPFRequestsIssued
		                                         + numberOfNeo4jRequestsIssued;
		put( enOverallNumberOfRequestsIssued, Long.valueOf(overallNumberOfRequestsIssued) );

		final long overallNumberOfRequestsCompleted = numberOfSPARQLRequestsCompleted
		                                            + numberOfTPFRequestsCompleted
		                                            + numberOfBRTPFRequestsCompleted
		                                            + numberOfNeo4jRequestsCompleted;
		put( enOverallNumberOfRequestsCompleted, Long.valueOf(overallNumberOfRequestsCompleted) );
	}

	@Override
	public long getOverallNumberOfRequestsIssued() { return (Long) getEntry(enOverallNumberOfRequestsIssued); }

	@Override
	public long getOverallNumberOfRequestsCompleted() { return (Long) getEntry(enOverallNumberOfRequestsCompleted); }

	@Override
	public long getNumberOfSPARQLRequestsIssued() { return (Long) getEntry(enNumberOfSPARQLRequestsIssued); }

	@Override
	public long getNumberOfTPFRequestsIssued() { return (Long) getEntry(enNumberOfTPFRequestsIssued); }

	@Override
	public long getNumberOfBRTPFRequestsIssued() { return (Long) getEntry(enNumberOfBRTPFRequestsIssued); }

	@Override
	public long getNumberOfNeo4jRequestsIssued() { return (Long) getEntry(enNumberOfNeo4jRequestsIssued); }

	@Override
	public long getNumberOfSPARQLRequestsCompleted() { return (Long) getEntry(enNumberOfSPARQLRequestsCompleted); }

	@Override
	public long getNumberOfTPFRequestsCompleted() { return (Long) getEntry(enNumberOfTPFRequestsCompleted); }

	@Override
	public long getNumberOfBRTPFRequestsCompleted() { return (Long) getEntry(enNumberOfBRTPFRequestsCompleted); }

	@Override
	public long getNumberOfNeo4jRequestsCompleted() { return (Long) getEntry(enNumberOfNeo4jRequestsCompleted); }

}
