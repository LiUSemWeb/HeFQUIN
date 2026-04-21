package se.liu.ida.hefquin.federation.access.impl;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
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
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCache;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheEntry;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheEntryFactory;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheKey;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheKey.ResponseMode;
import se.liu.ida.hefquin.federation.members.BRTPFServer;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.members.TPFServer;

/**
 * Federation access manager with a ChronicleMap-backed persistent cache.
 *
 * <p>
 * This class stores cache entries in a persistent {@link ChronicleMapCache} so
 * that successful responses can be reused across repeated requests and runs.
 * </p>
 *
 * <p>
 * On a cache miss, the request is delegated to the wrapped federation access
 * manager. Successful responses are converted into cache objects and written to
 * the persistent cache.
 * </p>
 *
 * <p>
 * Cache writes happen asynchronously and the returned futures represent the
 * underlying retrieval results. Cache population is performed after successful
 * completion of that future.
 * </p>
 *
 * <p>
 * Synchronization in this class is performed on the {@link ChronicleMapCache}
 * instance to avoid issuing duplicate requests concurrently. This is
 * independent of the internal synchronization used by the cache itself.
 * </p>
 */
public class FederationAccessManagerWithChronicleMapCache extends FederationAccessManagerWithCache implements AutoCloseable
{
	protected static final long DEFAULT_TIME_TO_LIVE = 300_000; // 5 minutes
	protected final ChronicleMapCache chronicleMapCache;

	/**
	 * Creates a federation access manager with a ChronicleMap-backed persistent
	 * cache using the given capacity and cache policies.
	 *
	 * @param fedAccMan     the wrapped federation access manager
	 * @param cacheCapacity the maximum cache capacity
	 * @throws IOException if the persistent cache cannot be created or opened
	 */
	public FederationAccessManagerWithChronicleMapCache( final FederationAccessManager fedAccMan,
	                                                     final int cacheCapacity,
	                                                     final CachePolicies<ChronicleMapCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>, ChronicleMapCacheEntry> chronicleMapCachePolicies )
			throws IOException
	{
		super(fedAccMan, cacheCapacity);
		chronicleMapCache = new ChronicleMapCache(cacheCapacity, chronicleMapCachePolicies);
	}

	/**
	 * Creates a federation access manager with a ChronicleMap-backed persistent
	 * cache using the given capacity and the default cache policies.
	 *
	 * <p>
	 * The cache uses a least-recently-used (LRU) replacement policy and the
	 * specified time-to-live for cache entry invalidation.
	 * </p>
	 *
	 * @param fedAccMan     the wrapped federation access manager
	 * @param cacheCapacity the maximum cache capacity
	 * @param timeToLive    the cache entry time-to-live in milliseconds
	 * @throws IOException if the persistent cache cannot be created or opened
	 */
	public FederationAccessManagerWithChronicleMapCache( final FederationAccessManager fedAccMan,
	                                                     final int cacheCapacity,
	                                                     final long timeToLive )
			throws IOException 
	{
		this(fedAccMan, cacheCapacity, new DefaultChronicleMapCachePolicies(timeToLive) );
	}

	/**
	 * Creates a federation access manager with a ChronicleMap-backed persistent
	 * cache using the given capacity and the default cache policies.
	 *
	 * <p>
	 * The default policies use least-recently-used (LRU) replacement and a
	 * time-to-live of {@value #DEFAULT_TIME_TO_LIVE} milliseconds.
	 * </p>
	 *
	 * @param fedAccMan     the wrapped federation access manager
	 * @param cacheCapacity the maximum cache capacity
	 * @throws IOException if the persistent cache cannot be created or opened
	 */
	public FederationAccessManagerWithChronicleMapCache( final FederationAccessManager fedAccMan,
	                                                     final int cacheCapacity )
			throws IOException 
	{
		this(fedAccMan, cacheCapacity, new DefaultChronicleMapCachePolicies(DEFAULT_TIME_TO_LIVE) );
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

		final ChronicleMapCacheKey key;
		try {
			key = new ChronicleMapCacheKey( req, fm, ResponseMode.RESULT );
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

		// We synchronize on the chronicleMapCache instance to prevent multiple threads
		// from issuing the same request concurrently and inserting duplicate
		// CompletableFutures. We cache the CompletableFuture immediately (before
		// completion), so that concurrent callers can share the same future. The actual
		// response data can only be persisted on disk once the future completes.
		synchronized (chronicleMapCache) {
			cachedResponse = chronicleMapCache.get(key);
			if ( cachedResponse == null ) {
				final CompletableFuture<RespType> newResponse = fedAccMan.issueRequest(req, fm);
				chronicleMapCache.put(key, newResponse);
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
	public < ReqType extends DataRetrievalRequest,
	         RespType extends DataRetrievalResponse<?>,
	         MemberType extends FederationMember >
	CompletableFuture<CardinalityResponse> issueCardReq( final ReqType req,
	                                                     final MemberType fm )
			throws FederationAccessException
	{
		final ChronicleMapCacheKey key;
		try {
			key = new ChronicleMapCacheKey( req, fm, ResponseMode.COUNT );
		} catch ( final IllegalArgumentException e ) {
			throw new IllegalStateException( "Failed to create cache key for request/member combination: "
					+ req.getClass().getName() + "/" + fm.getClass().getName(), e );
		}

		final CompletableFuture<?> cachedResponse;

		// We synchronize on the chronicleMapCache instance to prevent multiple threads
		// from issuing the same request concurrently and inserting duplicate
		// CompletableFutures. We cache the CompletableFuture immediately (before
		// completion), so that concurrent callers can share the same future. The actual
		// response data can only be persisted on disk once the future completes.
		synchronized (chronicleMapCache) {
			cachedResponse = chronicleMapCache.get(key);
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

				chronicleMapCache.put(key, newResponse);
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
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest( final SPARQLRequest req,
	                                                                       final SPARQLEndpoint fm )
			throws FederationAccessException
	{
		return issueCardReq(req, fm);
	}
	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest( final TPFRequest req,
	                                                                       final TPFServer fm )
			throws FederationAccessException
	{
		return issueCardReq(req, fm);
	}
	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest( final TPFRequest req,
	                                                                       final BRTPFServer fm )
			throws FederationAccessException
	{
		return issueCardReq(req, fm);
	}

	@Override
	public void close() {
		chronicleMapCache.close();
	}

	/**
	 * Default cache policies for {@link ChronicleMapCache}.
	 *
	 * <p>
	 * Uses a least-recently-used (LRU) replacement policy and a time-to-live-based
	 * invalidation policy.
	 * </p>
	 */
	public static class DefaultChronicleMapCachePolicies
				implements CachePolicies<ChronicleMapCacheKey,
				                         CompletableFuture<? extends DataRetrievalResponse<?>>,
				                         ChronicleMapCacheEntry>
	{
		final ChronicleMapCacheEntryFactory cef = new ChronicleMapCacheEntryFactory();

		final CacheReplacementPolicyFactory<ChronicleMapCacheKey,
		                                    CompletableFuture<? extends DataRetrievalResponse<?>>,
		                                    ChronicleMapCacheEntry
		                                   > crpf= new CacheReplacementPolicyFactory<>() {
			@Override
			public CacheReplacementPolicy<ChronicleMapCacheKey,
			                              CompletableFuture<? extends DataRetrievalResponse<?>>,
			                              ChronicleMapCacheEntry> create() {
				return new CacheReplacementPolicyLRU<>();
			}
		};

		final CacheInvalidationPolicy<ChronicleMapCacheEntry, CompletableFuture<? extends DataRetrievalResponse<?>>> cip;

		public DefaultChronicleMapCachePolicies( final long timeToLive ) {
			cip = new CacheInvalidationPolicyTimeToLive<>(timeToLive);
		}

		@Override
		public ChronicleMapCacheEntryFactory getEntryFactory() {
			return cef;
		}

		@Override
		public CacheReplacementPolicyFactory<ChronicleMapCacheKey,
		                                     CompletableFuture<? extends DataRetrievalResponse<?>>,
		                                     ChronicleMapCacheEntry> getReplacementPolicyFactory() {
			return crpf;
		}

		@Override
		public CacheInvalidationPolicy<ChronicleMapCacheEntry,
		                               CompletableFuture<? extends DataRetrievalResponse<?>>> getInvalidationPolicy() {
			return cip;
		}
	} // end of DefaultChronicleMapCachePolicies
}
