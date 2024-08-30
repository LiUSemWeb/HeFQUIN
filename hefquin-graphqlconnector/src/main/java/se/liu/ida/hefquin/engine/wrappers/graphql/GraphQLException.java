package se.liu.ida.hefquin.engine.wrappers.graphql;

public class GraphQLException extends Exception
{
	private static final long serialVersionUID = 6679812375154324447L;

	public GraphQLException( final String message,
	                         final Throwable cause ) {
		super(message, cause);
	}

	public GraphQLException( final String message ) {
		super(message);
	}

	public GraphQLException( final Throwable cause ) {
		super(cause);
	}

}
