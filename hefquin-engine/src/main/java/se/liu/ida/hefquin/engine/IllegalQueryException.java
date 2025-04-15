package se.liu.ida.hefquin.engine;

import org.apache.jena.query.Query;

/**
 * An exception for cases in which a query given to HeFQUIN turns out to
 * be somehow invalid. Notice that this is different from queries that
 * are valid but not (yet) supported (the exception for these cases is
 * {@link UnsupportedQueryException}).
 */
public class IllegalQueryException extends Exception
{
	private static final long serialVersionUID = -7049103605352597870L;
	protected final Query query;

	/**
	 * The given message should describe the particular issue with the given
	 * query and should be written in a way that it can be passed directly to
	 * the user.
	 */
	public IllegalQueryException( final Query query, final String msg ) {
		this(query, msg, null);
	}

	/**
	 * The given message should describe the particular issue with the given
	 * query and should be written in a way that it can be passed directly to
	 * the user.
	 */
	public IllegalQueryException( final Query query, final String msg, final Throwable cause ) {
		super(msg, cause);

		assert query != null;
		this.query = query;
	}

	public Query getQuery() { return query; }

}
