package se.liu.ida.hefquin.engine.wrappers.graphql.utils;

public class QueryExecutionException extends Exception
{
	private static final long serialVersionUID = -6937277621497349998L;

	public QueryExecutionException( final String message, final Exception e ) {
		super(message,e);
	}
}