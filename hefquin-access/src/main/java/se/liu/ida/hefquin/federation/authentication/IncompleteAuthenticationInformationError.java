package se.liu.ida.hefquin.federation.authentication;

public class IncompleteAuthenticationInformationError extends Exception
{
	public IncompleteAuthenticationInformationError( final String message ) {
		super(message);
	}
}
