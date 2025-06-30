package se.liu.ida.hefquin.engine.federation.access;

import se.liu.ida.hefquin.engine.federation.FederationMember;

public class UnsupportedOperationDueToRetrievalError extends FederationAccessException
{
	private static final long serialVersionUID = 1L;
	protected final Integer errorCode;

	public UnsupportedOperationDueToRetrievalError( final Integer errorCode,
	                                                final String message,
	                                                final DataRetrievalRequest req,
	                                                final FederationMember fm ) {
		super( message, req, fm );
		this.errorCode = errorCode;
	}

	public UnsupportedOperationDueToRetrievalError( final Integer errorCode,
	                                                final String message,
	                                                final Throwable cause,
	                                                final DataRetrievalRequest req,
	                                                final FederationMember fm ) {
		super( message, cause, req, fm );
		this.errorCode = errorCode;
	}

	public UnsupportedOperationDueToRetrievalError( final String message,
	                                                final Throwable cause,
	                                                final DataRetrievalRequest req,
	                                                final FederationMember fm ) {
		super( message, cause, req, fm );
		this.errorCode = null;
	}

	public UnsupportedOperationDueToRetrievalError( final String message,
	                                                final DataRetrievalRequest req,
	                                                final FederationMember fm ) {
		super( message, req, fm );
		this.errorCode = null;
	}

	public UnsupportedOperationDueToRetrievalError( final Throwable cause,
	                                                final DataRetrievalRequest req,
	                                                final FederationMember fm ) {
		super( cause, req, fm );
		this.errorCode = null;
	}

	public UnsupportedOperationDueToRetrievalError( final DataRetrievalRequest req,
	                                                final FederationMember fm ) {
		super( req, fm );
		this.errorCode = null;
	}

	public Integer getErrorStatusCode(){
		return errorCode;
	}
}
