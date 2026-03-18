package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.members.BRTPFServer;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.members.TPFServer;

/**
 * Cache key representing a concrete request issued to a specific federation
 * member.
 *
 * The key is derived from a canonical request URL constructed from the
 * combination of the {@link DataRetrievalRequest} and the
 * {@link FederationMember}. Two keys are considered equal if they produce the
 * same request URL.
 */
public class ChronicleMapCacheKey implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final String requestUrl;

	/**
	 * Creates a cache key from a given request and a federation member.
	 *
	 * @param req retrieval request
	 * @param fm  federation member
	 * @throws IllegalArgumentException if the request/member is unsupported
	 */
	public ChronicleMapCacheKey( final DataRetrievalRequest req, final FederationMember fm ) {
		assert req != null;
		assert fm != null;

		if (    req instanceof SPARQLRequest sparqlRequest
		     && fm instanceof SPARQLEndpoint sparqlEndpoint ) {
			final String query = sparqlRequest.getQuery().toString();
			final String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
			final String url = sparqlEndpoint.getURL();
			requestUrl = url + "?query=" + encodedQuery;
		}
		else if ( req instanceof TPFRequest tpfRequest ) {
			if ( fm instanceof TPFServer tpfServer )
				requestUrl = tpfServer.createRequestURL(tpfRequest);
			else if ( fm instanceof BRTPFServer brtpfServer )
				requestUrl = brtpfServer.createRequestURL(tpfRequest);
			else
				throw new IllegalArgumentException( "Unexpected type of server: " + fm.getClass().getName() );
		}
		else if (    req instanceof BRTPFRequest brtpfRequest
		          && fm instanceof BRTPFServer brtpfServer ) {
			requestUrl = brtpfServer.createRequestURL(brtpfRequest);
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
		final ChronicleMapCacheKey other = (ChronicleMapCacheKey) obj;
		return requestUrl.equals( other.requestUrl );
	}

	@Override
	public int hashCode() {
		return requestUrl.hashCode();
	}

	@Override
	public String toString() {
		return "ChronicleMapCacheKey{url='" + requestUrl + "'}";
	}
}
