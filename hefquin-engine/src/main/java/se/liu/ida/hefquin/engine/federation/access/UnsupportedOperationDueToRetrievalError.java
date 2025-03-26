package se.liu.ida.hefquin.engine.federation.access;

import se.liu.ida.hefquin.engine.federation.FederationMember;

public class UnsupportedOperationDueToRetrievalError extends FederationAccessException
{
	private static final long serialVersionUID = 1L;

	public UnsupportedOperationDueToRetrievalError( final String message,
	                                                final Throwable cause,
	                                                final DataRetrievalRequest req,
	                                                final FederationMember fm ) {
		super( message, cause, req, fm );
	}

	public UnsupportedOperationDueToRetrievalError( final String message,
	                                                final DataRetrievalRequest req,
	                                                final FederationMember fm ) {
		super( message, req,  fm );
	}

	public UnsupportedOperationDueToRetrievalError( final Throwable cause,
	                                                final DataRetrievalRequest req,
	                                                final FederationMember fm ) {
		super( cause, req, fm );
	}

	public UnsupportedOperationDueToRetrievalError( final DataRetrievalRequest req,
	                                                final FederationMember fm ) {
		super( req, fm );
	}
}
