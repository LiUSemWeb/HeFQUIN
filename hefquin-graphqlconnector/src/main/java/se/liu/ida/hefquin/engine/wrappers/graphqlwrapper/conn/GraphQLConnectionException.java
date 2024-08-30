package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.conn;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQLException;

public class GraphQLConnectionException extends GraphQLException
{
	private static final long serialVersionUID = 6679816875154327577L;

	public GraphQLConnectionException( final String message,
	                                   final Throwable cause ) {
		super(message, cause);
	}

	public GraphQLConnectionException( final String message ) {
		super(message);
	}

	public GraphQLConnectionException( final Throwable cause ) {
		super(cause);
	}

}
