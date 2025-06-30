package se.liu.ida.hefquin.federation.access.impl;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntry;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryFactory;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicyTimeToLive;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CachePolicies;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicyFactory;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicyLRU;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.federation.access.impl.cache.CardinalityCacheEntry;
import se.liu.ida.hefquin.federation.access.impl.cache.CardinalityCacheEntryFactory;
import se.liu.ida.hefquin.federation.access.impl.cache.CardinalityCacheKey;
import se.liu.ida.hefquin.federation.access.impl.cache.ChronicleMapCardinalityCache;
import se.liu.ida.hefquin.federation.access.impl.response.CachedCardinalityResponseImpl;

/**
 * A FederationAccessManager implementation that incorporates persistent disk
 * caching of cardinality requests.
 */
public class FederationAccessManagerWithChronicleMapCache extends FederationAccessManagerWithCache
{
	protected final ChronicleMapCardinalityCache cardinalityCache;
	protected final static int defaultCacheCapacity = 1000;

	public FederationAccessManagerWithChronicleMapCache( final FederationAccessManager fedAccMan,
	                                                     final int cacheCapacity,
	                                                     final CachePolicies<Key, CompletableFuture<? extends DataRetrievalResponse<?>>, ? extends CacheEntry<CompletableFuture<? extends DataRetrievalResponse<?>>>> cachePolicies,
	                                                     final CachePolicies<CardinalityCacheKey, Integer, CardinalityCacheEntry> cardinalityCachePolicies )
		throws IOException
	{
		super( fedAccMan, cacheCapacity, cachePolicies );
		cardinalityCache = new ChronicleMapCardinalityCache( cardinalityCachePolicies, cacheCapacity );
	}

	public FederationAccessManagerWithChronicleMapCache( final FederationAccessManager fedAccMan,
	                                                     final int cacheCapacity,
	                                                     final long timeToLive )
			throws IOException
	{
		this( fedAccMan,
		      cacheCapacity,
		      new MyDefaultCachePolicies(),
		      new MyDefaultCardinalityCachePolicies( timeToLive ) );
	}

	/**
	 * Creates a {@link FederationAccessManagerWithChronicleMapCache} with the default configuration.
	 */
	public FederationAccessManagerWithChronicleMapCache( final ExecutorService execService ) throws IOException
	{
		this( new AsyncFederationAccessManagerImpl( execService ),
		      defaultCacheCapacity,
		      new MyDefaultCachePolicies(),
		      new MyDefaultCardinalityCachePolicies() );
	}
	
	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest( final SPARQLRequest req,
	                                                                       final SPARQLEndpoint fm )
			throws FederationAccessException
	{
		final CardinalityCacheKey key = new CardinalityCacheKey( req, fm );
		final Date requestStartTime = new Date();
		final CardinalityCacheEntry cachedEntry = cardinalityCache.get( key );
		final Date requestEndTime = new Date();
		if ( cachedEntry != null ) {
			cacheHitsSPARQLCardinality++;
			final CardinalityResponse cr = new CachedCardinalityResponseImpl( cachedEntry.getObject(),
			                                                                  fm,
			                                                                  req,
			                                                                  requestStartTime,
			                                                                  requestEndTime );
			return CompletableFuture.completedFuture( cr );
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest( req, fm );
		newResponse.thenAccept( value -> {
			try {
				cardinalityCache.put( key, value.getCardinality() );
			} catch ( UnsupportedOperationDueToRetrievalError e ) {
				// intentionally ignored
			}
		} );
		return newResponse;
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest( final TPFRequest req,
	                                                                       final TPFServer fm )
			throws FederationAccessException
	{
		final CardinalityCacheKey key = new CardinalityCacheKey( req, fm );
		final Date requestStartTime = new Date();
		final CardinalityCacheEntry cachedEntry = cardinalityCache.get( key );
		final Date requestEndTime = new Date();
		if ( cachedEntry != null ) {
			cacheHitsTPFCardinality++;
			final CardinalityResponse cr = new CachedCardinalityResponseImpl( cachedEntry.getObject(),
			                                                                  fm,
			                                                                  req,
			                                                                  requestStartTime,
			                                                                  requestEndTime );
			return CompletableFuture.completedFuture( cr );
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest( req, fm );
		newResponse.thenAccept( value -> {
			try {
				cardinalityCache.put( key, value.getCardinality() );
			} catch ( UnsupportedOperationDueToRetrievalError e ) {
				// intentionally ignored
			}
		} );
		return newResponse;
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest( final TPFRequest req,
	                                                                       final BRTPFServer fm )
			throws FederationAccessException
	{
		final CardinalityCacheKey key = new CardinalityCacheKey( req, fm );
		final Date requestStartTime = new Date();
		final CardinalityCacheEntry cachedEntry = cardinalityCache.get( key );
		final Date requestEndTime = new Date();
		if ( cachedEntry != null ) {
			cacheHitsTPFCardinality++;
			final CardinalityResponse cr = new CachedCardinalityResponseImpl( cachedEntry.getObject(),
			                                                                  fm,
			                                                                  req,
			                                                                  requestStartTime,
			                                                                  requestEndTime );
			return CompletableFuture.completedFuture( cr );
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest( req, fm );
		newResponse.thenAccept( value -> {
			try {
				cardinalityCache.put( key, value.getCardinality() );
			} catch ( UnsupportedOperationDueToRetrievalError e ) {
				// intentionally ignored
			}
		} );
		return newResponse;
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest( final BRTPFRequest req,
	                                                                       final BRTPFServer fm )
			throws FederationAccessException
	{
		final CardinalityCacheKey key = new CardinalityCacheKey( req, fm );
		final Date requestStartTime = new Date();
		final CardinalityCacheEntry cachedEntry = cardinalityCache.get( key );
		final Date requestEndTime = new Date();
		if ( cachedEntry != null ) {
			cacheHitsTPFCardinality++;
			final CardinalityResponse cr = new CachedCardinalityResponseImpl( cachedEntry.getObject(),
			                                                                  fm,
			                                                                  req,
			                                                                  requestStartTime,
			                                                                  requestEndTime );
			return CompletableFuture.completedFuture( cr );
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest( req, fm );
		newResponse.thenAccept( value -> {
			try {
				cardinalityCache.put( key, value.getCardinality() );
			} catch ( UnsupportedOperationDueToRetrievalError e ) {
				// intentionally ignored
			}
		} );
		return newResponse;
	}

	/**
	 * Clears the persisted cardinality cache map.
	 */
	public void clearCardinalityCache() {
		cardinalityCache.clear();
	}

	protected static class MyDefaultCardinalityCachePolicies implements CachePolicies<CardinalityCacheKey, Integer, CardinalityCacheEntry>
	{
		protected final long timeToLive;
		protected final static long defaultTimeToLive = 300_000; // 5 minutes 

		public MyDefaultCardinalityCachePolicies() {
			this( defaultTimeToLive );
		}

		public MyDefaultCardinalityCachePolicies( final long timeToLive ) {
			this.timeToLive = timeToLive;
		}

		@Override
		public CacheEntryFactory<CardinalityCacheEntry, Integer> getEntryFactory() {
			return new CardinalityCacheEntryFactory();
		}

		@Override
		public CacheReplacementPolicyFactory<CardinalityCacheKey, Integer, CardinalityCacheEntry> getReplacementPolicyFactory() {
			return new CacheReplacementPolicyFactory<>() {
				@Override
				public CacheReplacementPolicy<CardinalityCacheKey, Integer, CardinalityCacheEntry> create() {
					return new CacheReplacementPolicyLRU<>();
				}
			};
		}

		@Override
		public CacheInvalidationPolicy<CardinalityCacheEntry, Integer> getInvalidationPolicy() {
			return new CacheInvalidationPolicyTimeToLive<>( timeToLive );
		}
	} // end of MyDefaultCachePolicies
}
