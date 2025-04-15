package se.liu.ida.hefquin.engine;

import org.apache.jena.query.Query;

/**
 * An exception for cases in which a query given to HeFQUIN uses some feature
 * that is not (yet) supported by HeFQUIN. Notice that this is different from
 * queries that are invalid (the exception for these cases is
 * {@link IllegalQueryException}).
 */
public class UnsupportedQueryException extends Exception
{
	private static final long serialVersionUID = -7979008960497975684L;
	protected final Query query;

	/**
	 * The given message should describe the specific limitation of HeFQUIN and
	 * should be written in a way that it can be passed directly to the user.
	 */
	public UnsupportedQueryException( final Query query, final String msg ) {
		this(query, msg, null);
	}

	/**
	 * The given message should describe the specific limitation of HeFQUIN and
	 * should be written in a way that it can be passed directly to the user.
	 */
	public UnsupportedQueryException( final Query query, final String msg, final Throwable cause ) {
		super(msg, cause);

		assert query != null;
		this.query = query;
	}

	public Query getQuery() { return query; }

}
