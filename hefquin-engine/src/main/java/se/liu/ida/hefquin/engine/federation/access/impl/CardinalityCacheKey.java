package se.liu.ida.hefquin.engine.federation.access.impl;

import java.io.Serializable;
import java.util.Objects;
import se.liu.ida.hefquin.base.query.SPARQLQuery;

/**
 * A key for caching cardinality requests, uniquely identified by a SPARQL
 * query and a target URL.
 */
public class CardinalityCacheKey implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String query;
	private final String url;

	/**
	 * Constructs a new CardinalityCacheKey using a SPARQL query and a URL.
	 *
	 * @param query the SPARQL query
	 * @param url   the endpoint URL
	 */
	public CardinalityCacheKey( final SPARQLQuery query, final String url ) {
		this.query = query.toString();
		this.url = url;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null || getClass() != obj.getClass() )
			return false;
		CardinalityCacheKey other = (CardinalityCacheKey) obj;
		return query.equals( other.query ) && url.equals( other.url );
	}

	@Override
	public int hashCode() {
		return Objects.hash( query, url );
	}

	@Override
	public String toString() {
		return "CardinalityCacheKey{query='" + query + "', url='" + url + "'}";
	}
}
