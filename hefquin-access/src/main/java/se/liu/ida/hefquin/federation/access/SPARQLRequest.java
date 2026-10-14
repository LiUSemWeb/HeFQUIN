package se.liu.ida.hefquin.federation.access;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLQuery;
import se.liu.ida.hefquin.base.query.impl.SPARQLQueryImpl;

public interface SPARQLRequest extends DataRetrievalRequest
{
	/**
	 * Returns the graph pattern for which solutions
	 * should be requested, or null if this request
	 * is based on an actual SPARQL query (to be
	 * accessed via the method {@link #getQuery()}.
	 */
	SPARQLGraphPattern getQueryPattern();

	/**
	 * Returns the set of variables that should be projected in the result,
	 * or {@code null} if no projection is specified for this request.
	 *
	 * <p>If a non-null set is returned (including the empty set), then
	 * projection is considered an explicit part of this request and must
	 * be respected by components that can support it.</p>
	 *
	 * <p>Deciding whether such a projection can be safely applied or pushed
	 * into a request is the responsibility of the query planning and
	 * rewriting logic, not of this interface or its implementations.</p>
	 */
	Set<Var> getProjectionVars();

	/**
	 * Returns {@code true} if this request <em>explicitly</em> requires
	 * that the requested result is duplicate free.
	 *
	 * @return {@code true} if duplicate elimination is requested;
	 *         {@code false} otherwise
	 */
	boolean getDistinctRequired();

	/**
	 * Returns the offset that should be applied to the result
	 * or 0 (zero) if no offset should be applied.
	 *
	 * @return a value of 0 (zero) or more
	 */
	int getOffset();

	/**
	 * Returns the (result size) limit that should be applied to the
	 * result or -1 if no limit should be applied.
	 *
	 * @return a value of -1 or more
	 */
	int getLimit();

	/**
	 * Returns the SPARQL query for which solutions
	 * should be requested.
	 */
	default SPARQLQuery getQuery() {
		return convertToQuery( getQueryPattern(),
		                       getProjectionVars(),
		                       getDistinctRequired(),
		                       getOffset(),
		                       getLimit() );
	}

	static SPARQLQuery convertToQuery( final SPARQLGraphPattern pattern,
	                                   final Set<Var> projectionVars,
	                                   final boolean distinctRequired,
	                                   final int offset,
	                                   final int limit ) {
		return new SPARQLQueryImpl(pattern, projectionVars, distinctRequired, offset, limit);
	}

}
