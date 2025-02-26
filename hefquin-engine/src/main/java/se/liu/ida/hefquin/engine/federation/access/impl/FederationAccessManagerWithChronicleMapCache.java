package se.liu.ida.hefquin.engine.federation.access.impl;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntry;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryFactory;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicyTimeToLive;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CachePolicies;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicyFactory;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.cache.CardinalityCacheEntry;
import se.liu.ida.hefquin.engine.federation.access.impl.cache.CardinalityCacheEntryFactory;
import se.liu.ida.hefquin.engine.federation.access.impl.cache.CardinalityCacheKey;
import se.liu.ida.hefquin.engine.federation.access.impl.cache.ChronicleMapCardinalityCache;
import se.liu.ida.hefquin.engine.federation.access.impl.response.CachedCardinalityResponseImpl;

/**
 * A FederationAccessManager implementation that incorporates persistent disk
 * caching of cardinality requests.
 */
public class FederationAccessManagerWithChronicleMapCache extends FederationAccessManagerWithCache
{
	protected final ChronicleMapCardinalityCache cardinalityCache;

	public FederationAccessManagerWithChronicleMapCache( final FederationAccessManager fedAccMan,
	                                                     final int cacheCapacity,
	                                                     final CachePolicies<Key, CompletableFuture<? extends DataRetrievalResponse>, ? extends CacheEntry<CompletableFuture<? extends DataRetrievalResponse>>> cachePolicies,
	                                                     final CachePolicies<CardinalityCacheKey, Integer, CardinalityCacheEntry> cardinalityCachePolicies )
		throws IOException
	{
		super( fedAccMan, cacheCapacity, cachePolicies );
		cardinalityCache = new ChronicleMapCardinalityCache( cardinalityCachePolicies );
	}

	public FederationAccessManagerWithChronicleMapCache( final FederationAccessManager fedAccMan,
	                                                     final int cacheCapacity,
	                                                     final long timeToLive )
		throws IOException
	{
		this( fedAccMan, cacheCapacity, new MyDefaultCachePolicies(), new MyDefaultCardinalityCachePolicies( timeToLive ) );
	}

	/**
	 * Creates a {@link FederationAccessManagerWithChronicleMapCache} with the default configuration.
	 */
	public FederationAccessManagerWithChronicleMapCache( final ExecutorService execService ) throws IOException
	{
		this( new AsyncFederationAccessManagerImpl( execService ), 1000, new MyDefaultCachePolicies(), new MyDefaultCardinalityCachePolicies() );
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
			final CardinalityResponse cr = new CachedCardinalityResponseImpl( fm,
			                                                                  req,
			                                                                  cachedEntry.getObject(),
			                                                                  requestStartTime,
			                                                                  requestEndTime );
			return CompletableFuture.completedFuture( cr );
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest( req, fm );
		newResponse.thenAccept(value -> {
			cardinalityCache.put( key,  value.getCardinality() );
		});
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
			final CardinalityResponse cr = new CachedCardinalityResponseImpl( fm,
			                                                                  req,
			                                                                  cachedEntry.getObject(),
			                                                                  requestStartTime,
			                                                                  requestEndTime );
			return CompletableFuture.completedFuture( cr );
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest( req, fm );
		newResponse.thenAccept(value -> {
			cardinalityCache.put( key,  value.getCardinality() );
		});
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
			final CardinalityResponse cr = new CachedCardinalityResponseImpl( fm,
			                                                                  req,
			                                                                  cachedEntry.getObject(),
			                                                                  requestStartTime,
			                                                                  requestEndTime );
			return CompletableFuture.completedFuture( cr );
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest( req, fm );
		newResponse.thenAccept(value -> {
			cardinalityCache.put( key,  value.getCardinality() );
		});
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
			final CardinalityResponse cr = new CachedCardinalityResponseImpl( fm,
			                                                                  req,
			                                                                  cachedEntry.getObject(),
			                                                                  requestStartTime,
			                                                                  requestEndTime );
			return CompletableFuture.completedFuture( cr );
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest( req, fm );
		newResponse.thenAccept(value -> {
			cardinalityCache.put( key,  value.getCardinality() );
		});
		return newResponse;
	}

	/**
	 * Clears the persisted cardinality cache map.
	 */
	public void clearCardinalityCache(){
		cardinalityCache.clear();
	}

	protected static class MyDefaultCardinalityCachePolicies implements CachePolicies<CardinalityCacheKey, Integer, CardinalityCacheEntry>
	{
		protected final long timeToLive;
		protected final static long defaultTimeToLive = 5 * 60 * 1000;

		public MyDefaultCardinalityCachePolicies(){
			this( defaultTimeToLive );
		}

		public MyDefaultCardinalityCachePolicies( final long timeToLive ){
			this.timeToLive = timeToLive;
		}

		@Override
		public CacheEntryFactory<CardinalityCacheEntry, Integer> getEntryFactory() {
			return new CardinalityCacheEntryFactory();
		}

		@Override
		public CacheReplacementPolicyFactory<CardinalityCacheKey, Integer, CardinalityCacheEntry> getReplacementPolicyFactory() {
			throw new UnsupportedOperationException("Unimplemented method 'getReplacementPolicyFactory'");
		}

		@Override
		public CacheInvalidationPolicy<CardinalityCacheEntry, Integer> getInvalidationPolicy() {
			return new CacheInvalidationPolicyTimeToLive<>( timeToLive );
		}
	} // end of MyDefaultCachePolicies
}
