package se.liu.ida.hefquin.federation.access;

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
