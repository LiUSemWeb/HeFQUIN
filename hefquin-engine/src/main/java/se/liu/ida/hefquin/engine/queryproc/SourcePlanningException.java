package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.base.query.Query;

/**
 * An exception that occurred when creating a source assignment for a given query.
 */
public class SourcePlanningException extends QueryPlanningException
{
	private static final long serialVersionUID = 6907355125900557975L;

	protected final Query query;

	public SourcePlanningException( final String message, final Throwable cause, final Query query ) {
		super(message, cause);
		this.query = query;
	}

	public SourcePlanningException( final String message, final Query query ) {
		super(message);
		this.query = query;
	}

	public SourcePlanningException( final Throwable cause, final Query query ) {
		super(cause);
		this.query = query;
	}

	public SourcePlanningException( final Query query ) {
		super();
		this.query = query;
	}

	/**
	 * Return the query for which the creation of
	 * a source assignment caused this exception.
	 */
	public Query getQuery() {
		return query;
	}

}
