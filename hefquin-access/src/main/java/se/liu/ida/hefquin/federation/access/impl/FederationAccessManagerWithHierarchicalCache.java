package se.liu.ida.hefquin.federation.access.impl;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.base.datastructures.Cache;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicyTimeToLive;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CachePolicies;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicyFactory;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicyLRU;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.impl.cache.HierarchicalCache;
import se.liu.ida.hefquin.federation.access.impl.cache.PersistentCacheEntry;
import se.liu.ida.hefquin.federation.access.impl.cache.PersistentCacheEntryFactory;
import se.liu.ida.hefquin.federation.access.impl.cache.PersistentCacheKey;
import se.liu.ida.hefquin.federation.access.impl.cache.PersistentCacheKey.ResponseMode;
import se.liu.ida.hefquin.federation.members.BRTPFServer;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.members.TPFServer;

/**
 * A federation access manager that uses a hierarchical cache to store
 * federation access results and cardinality estimates.
 *
 * <p>
 * The cache consists of two cache layers arranged hierarchically. Requests are
 * first looked up in the primary cache layer. If no cached entry is found, the
 * secondary cache layer is consulted. Entries retrieved from the secondary
 * layer are promoted to the primary layer to improve the performance of future
 * lookups.
 * </p>
 *
 * <p>
 * Both data retrieval requests and cardinality requests are cached. Cache keys
 * are represented by {@link PersistentCacheKey} objects, which uniquely
 * identify a request, the targeted federation member, and the type of cached
 * response.
 * </p>
 *
 * <p>
 * To reduce duplicate request execution, this class stores
 * {@link CompletableFuture} instances in the cache whenever possible. If the
 * underlying cache layers preserve the cached futures, concurrent requests for
 * the same data may share both the same future and the same underlying request
 * execution. However, some cache implementations may store serialized or
 * reconstructed representations of cached values rather than the original
 * future instance. In such cases, the degree to which concurrent requests can
 * be coalesced depends on the behavior of the underlying cache layers.
 * </p>
 *
 * <p>
 * Cache replacement and invalidation behavior are determined by the policies
 * configured for the underlying cache layers.
 * </p>
 */
public class FederationAccessManagerWithHierarchicalCache extends FederationAccessManagerWithCache
{
	protected final HierarchicalCache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, PersistentCacheEntry> cache;

	/**
	 * Creates a federation access manager with a ChronicleMap-backed persistent
	 * cache using the given capacity and cache policies.
	 *
	 * @param fedAccMan                 the wrapped federation access manager
	 * @param l1Cache
	 * @param l2Cache
	 */
	public FederationAccessManagerWithHierarchicalCache( final FederationAccessManager fedAccMan,
	                                                     final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> l1Cache,
	                                                     final Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> l2Cache )
			throws IOException
	{
		super(fedAccMan, 1);
		cache = new HierarchicalCache<>(l1Cache, l2Cache);
	}

	/**
	 * Issues a request for the given request and federation member.
	 *
	 * <p>
	 * Checks the cache first and returns a cached response if available. Otherwise,
	 * delegates the request to the wrapped federation access manager and caches the
	 * result asynchronously.
	 * </p>
	 *
	 * @param req the data retrieval request (TPF, BRTPF, or SPARQL)
	 * @param fm  the federation member handling the request
	 * @return a {@link CompletableFuture} with the response
	 *
	 * @throws FederationAccessException if the request fails
	 * @throws IllegalStateException     if the request/member combination is
	 *                                   unsupported
	 */
	@Override
	public < ReqType extends DataRetrievalRequest,
	         RespType extends DataRetrievalResponse<?>,
	         MemberType extends FederationMember >
	CompletableFuture<RespType> issueRequest( final ReqType req,
	                                          final MemberType fm )
			throws FederationAccessException
	{
		// update the statistics
		if ( req instanceof TPFRequest )
			cacheRequestsTPF++;
		else if ( req instanceof BRTPFRequest )
			cacheRequestsBRTPF++;
		else if ( req instanceof SPARQLRequest )
			cacheRequestsSPARQL++;
		else
			cacheRequestsOther++;

		final PersistentCacheKey key;
		try {
			key = new PersistentCacheKey( req, fm, ResponseMode.RESULT );
		} catch ( final IllegalArgumentException e ) {
			// TODO: Currently unsupported request/member types bypass the cache silently.
			// This may cause bugs in the future if full cache coverage is assumed later.
			// In a follow-up PR (#590):
			// (i) extend the cache implementation to support all request/member types, and
			// (ii) replace this fallback with an exception.
			return fedAccMan.issueRequest(req, fm);
			// throw new IllegalStateException( "Failed to create cache key for request/member combination: "
			// 		+ req.getClass().getName() + "/" + fm.getClass().getName(), e );
		}

		final CompletableFuture<?> cachedResponse;

		// We synchronize on the cache instance to prevent multiple threads
		// from issuing the same request concurrently and inserting duplicate
		// CompletableFutures. We cache the CompletableFuture immediately (before
		// completion), so that concurrent callers can share the same future. The actual
		// response data can only be persisted on disk once the future completes.
		synchronized (cache) {
			cachedResponse = cache.get(key);
			if ( cachedResponse == null ) {
				final CompletableFuture<RespType> newResponse = fedAccMan.issueRequest(req, fm);
				cache.put(key, newResponse);
				return newResponse;
			}
		}

		// cache hit, update the statistics
		if( req instanceof TPFRequest )
			cacheHitsTPF++;
		else if( req instanceof BRTPFRequest )
			cacheHitsBRTPF++;
		else if( req instanceof SPARQLRequest )
			cacheHitsSPARQL++;
		else
			throw new IllegalArgumentException("Unrecognized request type: " + req.getClass().getName());

		@SuppressWarnings("unchecked")
		final CompletableFuture<RespType> cachedResponse2 = (CompletableFuture<RespType>) cachedResponse;
		return cachedResponse2;
	}

	/**
	 * Issues a cardinality request for the given request and federation member.
	 *
	 * <p>
	 * Checks the cache first and returns a cached response if available. Otherwise,
	 * delegates the request to the wrapped federation access manager and caches the
	 * result asynchronously.
	 * </p>
	 *
	 * @param req the data retrieval request (TPF, BRTPF, or SPARQL)
	 * @param fm  the federation member handling the request
	 * @return a {@link CompletableFuture} with the cardinality response
	 *
	 * @throws FederationAccessException if the request fails
	 * @throws IllegalStateException     if the request/member combination is
	 *                                   unsupported
	 */
	@Override
	public < ReqType extends DataRetrievalRequest,
	         RespType extends DataRetrievalResponse<?>,
	         MemberType extends FederationMember >
	CompletableFuture<CardinalityResponse> issueCardinalityRequest(
			final ReqType req,
			final MemberType fm )
					throws FederationAccessException
	{
		final PersistentCacheKey key;
		try {
			key = new PersistentCacheKey( req, fm, ResponseMode.COUNT );
		} catch ( final IllegalArgumentException e ) {
			throw new IllegalStateException( "Failed to create cache key for request/member combination: "
					+ req.getClass().getName() + "/" + fm.getClass().getName(), e );
		}

		final CompletableFuture<?> cachedResponse;

		// We synchronize on the cache instance to prevent multiple threads
		// from issuing the same request concurrently and inserting duplicate
		// CompletableFutures. We cache the CompletableFuture immediately (before
		// completion), so that concurrent callers can share the same future.
		synchronized (cache) {
			cachedResponse = cache.get(key);
			if ( cachedResponse == null ) {
				final CompletableFuture<CardinalityResponse> newResponse;
				if (    req instanceof TPFRequest tpfReq
					 && fm instanceof TPFServer tpfServer )
					newResponse = fedAccMan.issueCardinalityRequest(tpfReq, tpfServer);
				else if (    req instanceof TPFRequest tpfReq
						  && fm instanceof BRTPFServer brtpfServer )
					newResponse = fedAccMan.issueCardinalityRequest(tpfReq, brtpfServer);
				else if (    req instanceof BRTPFRequest brtpfReq
						  && fm instanceof BRTPFServer brtpfServer )
					newResponse = fedAccMan.issueCardinalityRequest(brtpfReq, brtpfServer);
				else if (    req instanceof SPARQLRequest sparqlReq
						  && fm instanceof SPARQLEndpoint sparqlEndpoint )
					newResponse = fedAccMan.issueCardinalityRequest(sparqlReq, sparqlEndpoint);
				else
					throw new IllegalStateException( "Unsupported request/federation member combination: " +
													req.getClass().getName() + "/" + fm.getClass().getName() );

				cache.put(key, newResponse);
				return newResponse;
			}
		}

		// cache hit, update the statistics
		if( req instanceof TPFRequest )
			cacheHitsTPFCardinality++;
		else if( req instanceof BRTPFRequest )
			cacheHitsBRTPFCardinality++;
		else if( req instanceof SPARQLRequest )
			cacheHitsSPARQLCardinality++;
		else
			throw new IllegalArgumentException("Unrecognized request type: " + req.getClass().getName());

		@SuppressWarnings("unchecked")
		final CompletableFuture<CardinalityResponse> cachedResponse2 = (CompletableFuture<CardinalityResponse>) cachedResponse;
		return cachedResponse2;
	}

	@Override
	public void shutdown() {
		cache.close();
		super.shutdown();
	}

	/**
	 * Default cache policies for {@link HierarchicalCache}.
	 *
	 * <p>
	 * Uses a least-recently-used (LRU) replacement policy and a time-to-live-based
	 * invalidation policy.
	 * </p>
	 */
	public static class DefaultHierarchicalMapCachePolicies
				implements CachePolicies<PersistentCacheKey,
				                         CompletableFuture<? extends DataRetrievalResponse<?>>,
				                         PersistentCacheEntry>
	{
		final PersistentCacheEntryFactory cef = new PersistentCacheEntryFactory();

		final CacheReplacementPolicyFactory<PersistentCacheKey,
		                                    CompletableFuture<? extends DataRetrievalResponse<?>>,
		                                    PersistentCacheEntry
		                                   > crpf= new CacheReplacementPolicyFactory<>() {
			@Override
			public CacheReplacementPolicy<PersistentCacheKey,
			                              CompletableFuture<? extends DataRetrievalResponse<?>>,
			                              PersistentCacheEntry> create() {
				return new CacheReplacementPolicyLRU<>();
			}
		};

		final CacheInvalidationPolicy<PersistentCacheEntry, CompletableFuture<? extends DataRetrievalResponse<?>>> cip;

		/**
		 * Creates a policy configuration with the given cache entry lifetime.
		 *
		 * @param timeToLive maximum lifetime of a cache entry, in seconds, before it is
		 *                   considered invalid and eligible for eviction
		 */
		public DefaultHierarchicalMapCachePolicies( final int timeToLive ) {
			cip = new CacheInvalidationPolicyTimeToLive<>( timeToLive * 1000L );
		}

		@Override
		public PersistentCacheEntryFactory getEntryFactory() {
			return cef;
		}

		@Override
		public CacheReplacementPolicyFactory<PersistentCacheKey,
		                                     CompletableFuture<? extends DataRetrievalResponse<?>>,
		                                     PersistentCacheEntry> getReplacementPolicyFactory() {
			return crpf;
		}

		@Override
		public CacheInvalidationPolicy<PersistentCacheEntry,
		                               CompletableFuture<? extends DataRetrievalResponse<?>>> getInvalidationPolicy() {
			return cip;
		}
	} // end of DefaultHierarchicalMapCachePolicies
}
