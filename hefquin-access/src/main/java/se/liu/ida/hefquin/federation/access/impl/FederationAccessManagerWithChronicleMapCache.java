package se.liu.ida.hefquin.federation.access.impl;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntry;
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
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCache;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheEntry;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheEntryFactory;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheKey;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheObject;
import se.liu.ida.hefquin.federation.access.impl.response.CachedCardinalityResponseImpl;
import se.liu.ida.hefquin.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.federation.access.impl.response.TPFResponseImpl;
import se.liu.ida.hefquin.federation.members.BRTPFServer;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.members.TPFServer;

/**
 * Federation access manager that augments
 * {@link FederationAccessManagerWithCache} with a ChronicleMap-backed
 * persistent cache.
 *
 * Currently, persistent caching is implemented for solution-mapping responses.
 * On a cache miss, requests are delegated to the wrapped federation access
 * manager and successful responses are persisted for future reuse.
 */
public class FederationAccessManagerWithChronicleMapCache extends FederationAccessManagerWithCache implements AutoCloseable
{
	protected final ChronicleMapCache chronicleMapCache;

	public FederationAccessManagerWithChronicleMapCache( final FederationAccessManager fedAccMan,
	                                                     final int cacheCapacity,
	                                                     final CachePolicies<Key,CompletableFuture<? extends DataRetrievalResponse<?>>, ? extends CacheEntry<CompletableFuture<? extends DataRetrievalResponse<?>>>> cachePolicies,
	                                                     final CachePolicies<ChronicleMapCacheKey, ChronicleMapCacheObject, ChronicleMapCacheEntry> chronicleMapCachePolicies )
			throws IOException
	{
		super(fedAccMan, cacheCapacity, cachePolicies);
		chronicleMapCache = new ChronicleMapCache(chronicleMapCachePolicies);
	}

	public FederationAccessManagerWithChronicleMapCache( final FederationAccessManager fedAccMan,
	                                                     final int cacheCapacity,
	                                                     final CachePolicies<Key, CompletableFuture<? extends DataRetrievalResponse<?>>, ? extends CacheEntry<CompletableFuture<? extends DataRetrievalResponse<?>>>> cachePolicies )
			throws IOException 
	{
		this(fedAccMan, cacheCapacity, cachePolicies, new DefaultChronicleMapCachePolicies());
	}

	public FederationAccessManagerWithChronicleMapCache( final FederationAccessManager fedAccMan,
	                                                     final int cacheCapacity )
			throws IOException 
	{
		this( fedAccMan, cacheCapacity, new MyDefaultCachePolicies() );
	}

	public FederationAccessManagerWithChronicleMapCache( final ExecutorService execService )
			throws Exception 
	{
		this( new AsyncFederationAccessManagerImpl(execService),
		      100,
		      new MyDefaultCachePolicies(),
			  new DefaultChronicleMapCachePolicies() );
	}

	/**
	 * Issues a request for the given request and federation member.
	 *
	 * Checks the cache first and returns a cached response if available. Otherwise,
	 * delegates the request to the federation access manager and caches the result
	 * asynchronously.
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

		final Date accessStartTime = new Date();
		final ChronicleMapCacheKey key = new ChronicleMapCacheKey( req, fm, ChronicleMapCacheKey.ResponseMode.RESULT );
		final ChronicleMapCacheObject cachedObject = chronicleMapCache.get(key);

		// Check cache
		if ( cachedObject != null ) {
			if( req instanceof TPFRequest )
				cacheHitsTPF++;
			else if( req instanceof BRTPFRequest )
				cacheHitsBRTPF++;
			else if( req instanceof SPARQLRequest )
				cacheHitsSPARQL++;

			final DataRetrievalResponse<?> cachedResponse;
			if (    req instanceof TPFRequest
			     || req instanceof BRTPFRequest )
				cachedResponse = new TPFResponseImpl( cachedObject.getMatchingTriples(),
				                                      cachedObject.getMetadataTriples(),
				                                      cachedObject.getNextPageURL(),
				                                      fm,
				                                      req,
				                                      accessStartTime,
				                                      new Date() );
			else if ( req instanceof SPARQLRequest )
				cachedResponse = new SolMapsResponseImpl( cachedObject.getSolutionMappings(),
				                                          fm,
				                                          req,
				                                          accessStartTime,
				                                          new Date() );
			else
				throw new IllegalStateException( "Unsupported request type: " + req.getClass().getName() );

			@SuppressWarnings("unchecked")
			final CompletableFuture<RespType> cachedResponse2 = (CompletableFuture<RespType>) CompletableFuture
					.completedFuture(cachedResponse);
			return cachedResponse2;
		}

		// Issue request and add it to the cache
		final CompletableFuture<RespType> newResponse = fedAccMan.issueRequest(req, fm);
		newResponse.thenAccept( value -> {
			try {
				final ChronicleMapCacheObject object = ChronicleMapCacheObject.create(value);
				chronicleMapCache.put(key, object);
			} catch ( UnsupportedOperationDueToRetrievalError e ) {
				throw new CompletionException(e);
			}
		} );
		return newResponse;
	}

	/**
	 * Issues a cardinality request for the given request and federation member.
	 *
	 * Checks the cache first and returns a cached response if available. Otherwise,
	 * delegates the request to the federation access manager and caches the result
	 * asynchronously.
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
		final Date accessStartTime = new Date();
		final ChronicleMapCacheKey key = new ChronicleMapCacheKey( req, fm, ChronicleMapCacheKey.ResponseMode.COUNT );
		final ChronicleMapCacheObject cachedObject = chronicleMapCache.get(key);

		// Check cache
		if ( cachedObject != null ) {
			if( req instanceof TPFRequest )
				cacheHitsTPFCardinality++;
			else if( req instanceof BRTPFRequest )
				cacheHitsBRTPFCardinality++;
			else if( req instanceof SPARQLRequest )
				cacheHitsSPARQLCardinality++;

			final int count = cachedObject.getCount();
			CardinalityResponse cachedResponse = new CachedCardinalityResponseImpl( count,
			                                                                        fm,
			                                                                        req,
			                                                                        accessStartTime,
			                                                                        new Date() );
			return (CompletableFuture<CardinalityResponse>) CompletableFuture.completedFuture( cachedResponse );
		}

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

		newResponse.thenAccept( value -> {
			try {
				chronicleMapCache.put( key, new ChronicleMapCacheObject( value.getCardinality() ) );
			} catch ( UnsupportedOperationDueToRetrievalError e ) {
				// intentionally ignored
			}
		} );
		return newResponse;
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

	public static class DefaultChronicleMapCachePolicies
				implements CachePolicies<ChronicleMapCacheKey,
				                         ChronicleMapCacheObject,
				                         ChronicleMapCacheEntry>
	{
		final ChronicleMapCacheEntryFactory cef = new ChronicleMapCacheEntryFactory();

		final CacheReplacementPolicyFactory<ChronicleMapCacheKey,
		                                    ChronicleMapCacheObject,
		                                    ChronicleMapCacheEntry
		                                   > crpf= new CacheReplacementPolicyFactory<>() {
			@Override
			public CacheReplacementPolicy<ChronicleMapCacheKey,
			                              ChronicleMapCacheObject,
			                              ChronicleMapCacheEntry> create() {
				return new CacheReplacementPolicyLRU<>();
			}
		};

		final CacheInvalidationPolicy<ChronicleMapCacheEntry,
		                              ChronicleMapCacheObject
		                             > cip = new CacheInvalidationPolicyTimeToLive<>(600_000); // 5 minutes

		@Override
		public ChronicleMapCacheEntryFactory getEntryFactory() {
			return cef;
		}

		@Override
		public CacheReplacementPolicyFactory<ChronicleMapCacheKey,
		                                     ChronicleMapCacheObject,
		                                     ChronicleMapCacheEntry> getReplacementPolicyFactory() {
			return crpf;
		}

		@Override
		public CacheInvalidationPolicy<ChronicleMapCacheEntry,
		                               ChronicleMapCacheObject> getInvalidationPolicy() {
			return cip;
		}
	} // end of MyDefaultCachePolicies
}
