package se.liu.ida.hefquin.federation.access;

public class CardinalityEstimationUnavailableError extends FederationAccessException
{
	private static final long serialVersionUID = 1L;

	public CardinalityEstimationUnavailableError( final String message,
	                                              final Throwable cause ) {
		super( message, cause, null, null );
	}

	public CardinalityEstimationUnavailableError( final String message ) {
		super( message, null, null );
	}

	public CardinalityEstimationUnavailableError( final Throwable cause ) {
		super( cause, null, null );
	}

	public CardinalityEstimationUnavailableError() {
		super( null, null );
	}
}
