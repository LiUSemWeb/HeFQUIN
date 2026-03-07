package se.liu.ida.hefquin.federation.access.impl;

import se.liu.ida.hefquin.base.utils.StatsImpl;
import se.liu.ida.hefquin.federation.access.FederationAccessStats;

public class FederationAccessStatsImpl extends StatsImpl implements FederationAccessStats
{
	protected static final String enNumberOfSPARQLRequestsIssued   = "numberOfSPARQLRequestsIssued";
	protected static final String enNumberOfTPFRequestsIssued      = "numberOfTPFRequestsIssued";
	protected static final String enNumberOfBRTPFRequestsIssued    = "numberOfBRTPFRequestsIssued";
	protected static final String enNumberOfOtherRequestsIssued    = "numberOfOtherRequestsIssued";

	protected static final String enNumberOfSPARQLRequestsCompleted   = "numberOfSPARQLRequestsCompleted";
	protected static final String enNumberOfTPFRequestsCompleted      = "numberOfTPFRequestsCompleted";
	protected static final String enNumberOfBRTPFRequestsCompleted    = "numberOfBRTPFRequestsCompleted";
	protected static final String enNumberOfOtherRequestsCompleted    = "numberOfOtherRequestsCompleted";

	protected static final String enOverallNumberOfRequestsIssued    = "overallNumberOfRequestsIssued";
	protected static final String enOverallNumberOfRequestsCompleted  = "overallNumberOfRequestsCompleted";

	public FederationAccessStatsImpl( final long numberOfSPARQLRequestsIssued,
	                                  final long numberOfTPFRequestsIssued,
	                                  final long numberOfBRTPFRequestsIssued,
	                                  final long numberOfOtherRequestsIssued,
	                                  final long numberOfSPARQLRequestsCompleted,
	                                  final long numberOfTPFRequestsCompleted,
	                                  final long numberOfBRTPFRequestsCompleted,
	                                  final long numberOfOtherRequestsCompleted ) {
		put( enNumberOfSPARQLRequestsIssued,  Long.valueOf(numberOfSPARQLRequestsIssued) );
		put( enNumberOfTPFRequestsIssued,     Long.valueOf(numberOfTPFRequestsIssued) );
		put( enNumberOfBRTPFRequestsIssued,   Long.valueOf(numberOfBRTPFRequestsIssued) );
		put( enNumberOfOtherRequestsIssued,   Long.valueOf(numberOfOtherRequestsIssued) );

		put( enNumberOfSPARQLRequestsCompleted,  Long.valueOf(numberOfSPARQLRequestsCompleted) );
		put( enNumberOfTPFRequestsCompleted,     Long.valueOf(numberOfTPFRequestsCompleted) );
		put( enNumberOfBRTPFRequestsCompleted,   Long.valueOf(numberOfBRTPFRequestsCompleted) );
		put( enNumberOfOtherRequestsCompleted,   Long.valueOf(numberOfOtherRequestsCompleted) );

		final long overallNumberOfRequestsIssued = numberOfSPARQLRequestsIssued
		                                         + numberOfTPFRequestsIssued
		                                         + numberOfBRTPFRequestsIssued
		                                         + numberOfOtherRequestsIssued;
		put( enOverallNumberOfRequestsIssued, Long.valueOf(overallNumberOfRequestsIssued) );

		final long overallNumberOfRequestsCompleted = numberOfSPARQLRequestsCompleted
		                                            + numberOfTPFRequestsCompleted
		                                            + numberOfBRTPFRequestsCompleted
		                                            + numberOfOtherRequestsCompleted;
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
	public long getNumberOfOtherRequestsIssued() { return (Long) getEntry(enNumberOfOtherRequestsIssued); }

	@Override
	public long getNumberOfSPARQLRequestsCompleted() { return (Long) getEntry(enNumberOfSPARQLRequestsCompleted); }

	@Override
	public long getNumberOfTPFRequestsCompleted() { return (Long) getEntry(enNumberOfTPFRequestsCompleted); }

	@Override
	public long getNumberOfBRTPFRequestsCompleted() { return (Long) getEntry(enNumberOfBRTPFRequestsCompleted); }

	@Override
	public long getNumberOfOtherRequestsCompleted() { return (Long) getEntry(enNumberOfOtherRequestsCompleted); }

}
