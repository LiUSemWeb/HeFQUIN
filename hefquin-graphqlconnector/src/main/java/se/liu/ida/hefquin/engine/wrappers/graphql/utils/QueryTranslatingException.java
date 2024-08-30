package se.liu.ida.hefquin.engine.wrappers.graphql.utils;

public class QueryTranslatingException extends Exception
{
	private static final long serialVersionUID = 2899662611655019879L;

	public QueryTranslatingException( final String message, final Exception e ) {
		super(message,e);
	}

	public QueryTranslatingException( final String message ) {
		super(message);
	}
}
