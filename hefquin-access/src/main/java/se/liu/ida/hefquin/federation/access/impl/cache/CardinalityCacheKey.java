package se.liu.ida.hefquin.federation.access.impl.cache;

import java.io.Serializable;
import java.util.Objects;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.members.BRTPFServer;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.members.TPFServer;

/**
 * A key for caching cardinality requests, uniquely identified by a
 * {@link DataRetrievalRequest} and a {@link FederationMember}.
 */
public class CardinalityCacheKey implements Serializable
{
	private static final long serialVersionUID = 1L;

	protected final String query;
	protected final String url;
	protected final String bindings;

	public CardinalityCacheKey( final DataRetrievalRequest req, final FederationMember fm ) {
		if ( req instanceof SPARQLRequest sparqlRequest && fm instanceof SPARQLEndpoint sparqlEndpoint ) {
			query = sparqlRequest.toString();
			url = sparqlEndpoint.getURL();
			bindings = "";
		}
		else if ( req instanceof TPFRequest tpfRequest ) {
			query = tpfRequest.toString();
			bindings = "";
			if ( fm instanceof TPFServer tpfServer )
				url = tpfServer.createRequestURL( tpfRequest );
			else if ( fm instanceof BRTPFServer brtpfServer )
				url = brtpfServer.createRequestURL( tpfRequest );
			else
				throw new IllegalArgumentException( "Unexpected type of server: " + fm.getClass().getName() );

		}
		else if ( req instanceof BRTPFRequest brtpfRequest && fm instanceof BRTPFServer brtpfServer ) {
			query = brtpfRequest.getTriplePattern().toString();
			url = brtpfServer.createRequestURL( brtpfRequest );
			bindings = brtpfRequest.getSolutionMappings().toString();
		}
		else {
			throw new IllegalArgumentException( "Unexpected request type: " + req.getClass().getName()
					+ "(server type: " + fm.getClass().getName() + ")" );
		}
	}

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null || getClass() != obj.getClass() )
			return false;
		final CardinalityCacheKey other = (CardinalityCacheKey) obj;
		return query.equals( other.query ) && url.equals( other.url ) && bindings.equals( other.bindings );
	}

	@Override
	public int hashCode() {
		return Objects.hash( query, url, bindings.hashCode() );
	}

	@Override
	public String toString() {
		return "CardinalityCacheKey{query='" + query + "', url='" + url + "', bindings='" + bindings + "'}";
	}
}
