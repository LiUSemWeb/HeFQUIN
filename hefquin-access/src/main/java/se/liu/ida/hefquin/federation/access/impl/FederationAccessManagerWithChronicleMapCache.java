package se.liu.ida.hefquin.federation.access.impl;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntry;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicyAlwaysValid;
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
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCache;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheEntry;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheEntryFactory;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheKey;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheObject;
import se.liu.ida.hefquin.federation.access.impl.response.CachedCardinalityResponseImpl;
import se.liu.ida.hefquin.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.federation.access.impl.response.TPFResponseImpl;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;

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
	private final ChronicleMapCache chronicleMapCache;

	public FederationAccessManagerWithChronicleMapCache( final FederationAccessManager fedAccMan,
	                                           final int cacheCapacity,
	                                           final CachePolicies<Key,CompletableFuture<? extends DataRetrievalResponse<?>>, ? extends CacheEntry<CompletableFuture<? extends DataRetrievalResponse<?>>>> cachePolicies,
	                                           final CachePolicies<ChronicleMapCacheKey, ChronicleMapCacheObject, ChronicleMapCacheEntry> chronicleMapCachePolicies )
			throws IOException 
	{
		super(fedAccMan, cacheCapacity, cachePolicies);
		chronicleMapCache = new ChronicleMapCache(cacheCapacity, chronicleMapCachePolicies);
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
	 * Issues the given request to the specified federation member, using the
	 * ChronicleMap-backed cache when possible.
	 *
	 * If a cached response is available, a completed future containing the
	 * reconstructed response is returned immediately. Otherwise, the request is
	 * delegated to the wrapped federation access manager and cacheable successful
	 * responses are persisted asynchronously after completion.
	 *
	 * @throws FederationAccessException if request processing fails before a future
	 *                                   can be returned
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
			     || req instanceof BRTPFRequest ) {
				cachedResponse = new TPFResponseImpl( cachedObject.getMatchingTriples(),
				                                      cachedObject.getMetadataTriples(),
				                                      cachedObject.getNextPageURL(),
				                                      fm,
				                                      req,
				                                      accessStartTime,
				                                      new Date() );
			}
			else if ( req instanceof SPARQLRequest ){
				cachedResponse = new SolMapsResponseImpl( cachedObject.getSolutionMappings(),
				                                          fm,
				                                          req,
				                                          accessStartTime,
				                                          new Date() );
			}
			else {
				throw new IllegalStateException( "Unsupported request type: " + req.getClass().getName() );
			}

			@SuppressWarnings("unchecked")
			final CompletableFuture<RespType> cachedResponse2 = (CompletableFuture<RespType>) CompletableFuture
					.completedFuture(cachedResponse);
			return cachedResponse2;
		}

		final CompletableFuture<RespType> newResponse = fedAccMan.issueRequest(req, fm);
		newResponse.thenAccept( value -> {
			try {
				final ChronicleMapCacheObject object = ChronicleMapCacheObject.create(value);
				if ( object != null ) {
					chronicleMapCache.put(key, object);
				}
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
		// Check cache
		final Date accessStartTime = new Date();
		final ChronicleMapCacheKey key = new ChronicleMapCacheKey( req, fm, ChronicleMapCacheKey.ResponseMode.COUNT );
		final ChronicleMapCacheObject cachedObject = chronicleMapCache.get(key);

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

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest(req, fm);

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
		                              ChronicleMapCacheObject> cip = new CacheInvalidationPolicyAlwaysValid<>();

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
