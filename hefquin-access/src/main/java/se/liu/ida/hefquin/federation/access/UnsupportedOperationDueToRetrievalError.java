package se.liu.ida.hefquin.federation.access;

public class UnsupportedOperationDueToRetrievalError extends FederationAccessException
{
	private static final long serialVersionUID = 1L;
	protected final Integer errorCode;

	public UnsupportedOperationDueToRetrievalError( final Integer errorCode,
	                                                final String message ) {
		super( message, null, null );
		this.errorCode = errorCode;
	}

	public UnsupportedOperationDueToRetrievalError( final Integer errorCode,
	                                                final String message,
	                                                final Throwable cause ) {
		super( message, cause, null, null );
		this.errorCode = errorCode;
	}

	public UnsupportedOperationDueToRetrievalError( final String message,
	                                                final Throwable cause ) {
		super( message, cause, null, null );
		this.errorCode = null;
	}

	public UnsupportedOperationDueToRetrievalError( final String message ) {
		super( message, null, null );
		this.errorCode = null;
	}

	public UnsupportedOperationDueToRetrievalError( final Throwable cause ) {
		super( cause, null, null );
		this.errorCode = null;
	}

	public UnsupportedOperationDueToRetrievalError() {
		super( null, null );
		this.errorCode = null;
	}

	public Integer getErrorStatusCode(){
		return errorCode;
	}
}
