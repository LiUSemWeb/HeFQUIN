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
	 * Returns the set of variables that should be projected in the result.
	 *
	 * <p>If non-empty, this set represents a request-level projection that may
	 * be applied when constructing the SPARQL query sent to an endpoint.
	 * Implementations may ignore this information if applying it would not be
	 * semantically safe (e.g., for queries with aggregation or expression-based
	 * projections).</p>
	 *
	 */
	Set<Var> getProjectionVars();

	/**
	 * Returns {@code true} if this request <em>explicit</em> requires that the requested
	 * result is duplicate free.
	 *
	 * @return {@code true} if duplicate elimination is requested; {@code false} otherwise
	 */
	boolean isDistinct();

	/**
	 * Returns the SPARQL query for which solutions
	 * should be requested.
	 */
	default SPARQLQuery getQuery() {
		return convertToQuery( getQueryPattern() );
	}

	static SPARQLQuery convertToQuery( final SPARQLGraphPattern pattern ) {
		return new SPARQLQueryImpl( pattern );
	}

}
