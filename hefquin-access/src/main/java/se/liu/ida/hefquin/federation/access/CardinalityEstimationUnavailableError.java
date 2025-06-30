package se.liu.ida.hefquin.federation.access;

import se.liu.ida.hefquin.federation.FederationMember;

public class CardinalityEstimationUnavailableError extends FederationAccessException
{
	private static final long serialVersionUID = 1L;

	public CardinalityEstimationUnavailableError( final String message,
	                                              final Throwable cause,
	                                              final DataRetrievalRequest req,
	                                              final FederationMember fm ) {
		super( message, cause, req, fm );
	}

	public CardinalityEstimationUnavailableError( final String message,
	                                              final DataRetrievalRequest req,
	                                              final FederationMember fm ) {
		super( message, req, fm );
	}

	public CardinalityEstimationUnavailableError( final Throwable cause,
	                                              final DataRetrievalRequest req,
	                                              final FederationMember fm ) {
		super( cause, req, fm );
	}

	public CardinalityEstimationUnavailableError( final DataRetrievalRequest req,
	                                              final FederationMember fm ) {
		super( req, fm );
	}
}
