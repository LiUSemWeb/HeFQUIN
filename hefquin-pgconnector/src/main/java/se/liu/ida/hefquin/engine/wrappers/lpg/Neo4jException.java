package se.liu.ida.hefquin.engine.wrappers.lpg;

public class Neo4jException extends Exception
{
	private static final long serialVersionUID = 2396752903306469745L;

	public Neo4jException( final String message ) {
		super(message);
	}

	public Neo4jException( final String message, final Throwable cause ) {
		super(message, cause);
	}

	public Neo4jException( final Throwable cause ) {
		super(cause);
	}

}
