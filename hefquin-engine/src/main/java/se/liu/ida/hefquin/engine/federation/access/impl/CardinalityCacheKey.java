package se.liu.ida.hefquin.engine.federation.access.impl;

import java.io.Serializable;
import java.util.Objects;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;

/**
 * A key for caching cardinality requests, uniquely identified by a {@link DataRetrievalRequest} and a {@link FederationMember}.
 */
public class CardinalityCacheKey implements Serializable {
	private static final long serialVersionUID = 1L;

	protected String query;
	protected String url;
	protected String bindings;

	public CardinalityCacheKey( final DataRetrievalRequest req, final FederationMember fm ) {
		// Default field values to ensure they are always initialized
		this.query = req.toString();
		this.url = fm.toString();
		this.bindings = "";

		if ( req instanceof SPARQLRequest sparqlRequest && fm instanceof SPARQLEndpoint sparqlEndpoint ) {
			query = sparqlRequest.toString();
			url = sparqlEndpoint.getInterface().getURL();
		} else if ( req instanceof TPFRequest tpfRequest ) {
			query = tpfRequest.toString();
			if ( fm instanceof TPFServer tpfServer ) {
				url = tpfServer.getInterface().createRequestURL( tpfRequest );
			} else if ( fm instanceof BRTPFServer brtpfServer ) {
				url = brtpfServer.getInterface().createRequestURL( tpfRequest );
			}
		} else if ( req instanceof BRTPFRequest brtpfRequest && fm instanceof BRTPFServer brtpfServer ) {
			query = brtpfRequest.getTriplePattern().toString();
			url = brtpfServer.getInterface().createRequestURL( brtpfRequest );
			bindings = brtpfRequest.getSolutionMappings().toString();
		}
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
		return "CardinalityCacheKey{query='" + query + "', url='" + url + "', bindings='" + bindings + "'}";
	}
}
