package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.apache.commons.codec.digest.DigestUtils;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.members.BRTPFServer;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.members.TPFServer;

/**
 * Immutable cache key for a request sent to a specific federation member.
 *
 * The key is derived from a string-based representation of the request,
 * constructed from the {@link DataRetrievalRequest}, the
 * {@link FederationMember}, and the {@link ResponseMode}.
 *
 * This representation is hashed using SHA-256 and stored as a 32-byte digest,
 * and equality and hash code are based solely on this digest.
 */
public class ChronicleMapCacheKey implements Serializable {
	private static final long serialVersionUID = 1L;

	// Binary SHA-256 digest (32 bits)
	private final byte[] requestDigest;
	public enum ResponseMode { RESULT, COUNT }

	/**
	 * Creates a cache key for the given request, federation member, and response
	 * mode.
	 *
	 * The key is computed by constructing a URL representation of the request and
	 * hashing it together with the response mode using SHA-256.
	 *
	 * @param req          the retrieval request (must not be {@code null})
	 * @param fm           the target federation member (must not be {@code null})
	 * @param responseMode the response mode (must not be {@code null})
	 *
	 * @throws IllegalArgumentException if the request/member combination is
	 *                                  unsupported
	 */
	public ChronicleMapCacheKey( final DataRetrievalRequest req,
	                             final FederationMember fm,
	                             final ResponseMode responseMode ) {
		assert req != null;
		assert fm != null;
		assert responseMode != null;

		final String requestUrl;

		if (    req instanceof SPARQLRequest sparqlRequest
		     && fm instanceof SPARQLEndpoint sparqlEndpoint ) {
			final String query = sparqlRequest.getQuery().toString();
			final String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
			requestUrl = sparqlEndpoint.getURL() + "?query=" + encodedQuery;
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

		requestDigest = DigestUtils.sha256( requestUrl + ";" + responseMode );
	}

	/**
	 * Compares this key with another object for equality.
	 *
	 * Two {@code ChronicleMapCacheKey} instances are considered equal if their
	 * SHA-256 digests are identical.
	 */
	@Override
	public boolean equals( final Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null || getClass() != obj.getClass() )
			return false;
		final ChronicleMapCacheKey other = (ChronicleMapCacheKey) obj;
		return Arrays.equals(requestDigest, other.requestDigest);
	}

	/**
	 * Returns a hash code derived from the SHA-256 digest.
	 *
	 * @return the hash code of this key
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(requestDigest);
	}

	@Override
	public String toString() {
		return "ChronicleMapCacheKey{" + requestDigest + "}";
	}
}
