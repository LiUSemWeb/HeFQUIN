package se.liu.ida.hefquin.engine.federation.access;

import se.liu.ida.hefquin.engine.federation.FederationMember;

public class UnsupportedOperationDueToRetrievalError extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	private final DataRetrievalRequest req;
	private final FederationMember fm;

	public UnsupportedOperationDueToRetrievalError( final String message,
	                                                final Throwable cause,
	                                                final DataRetrievalRequest req,
	                                                final FederationMember fm ) {
		super( message, cause );
		this.req = req;
		this.fm = fm;
	}

	public UnsupportedOperationDueToRetrievalError( final String message,
	                                                final DataRetrievalRequest req,
	                                                final FederationMember fm ) {
		super( message );
		this.req = req;
		this.fm = fm;
	}

	public UnsupportedOperationDueToRetrievalError( final Throwable cause,
	                                                final DataRetrievalRequest req,
	                                                final FederationMember fm ) {
		super( cause );
		this.req = req;
		this.fm = fm;
	}

	public UnsupportedOperationDueToRetrievalError( final DataRetrievalRequest req,
	                                                final FederationMember fm ) {
		this.req = req;
		this.fm = fm;
	}
}
