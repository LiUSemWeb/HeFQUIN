package se.liu.ida.hefquin.engine;

import org.apache.jena.query.Query;

public class UnsupportedQueryException extends Exception
{
	private static final long serialVersionUID = -7979008960497975684L;
	protected final Query query;

	public UnsupportedQueryException( final Query query, final String msg ) {
		this(query, msg, null);
	}

	public UnsupportedQueryException( final Query query, final String msg, final Throwable cause ) {
		super(msg, cause);

		assert query != null;
		this.query = query;
	}

	public Query getQuery() { return query; }

}
