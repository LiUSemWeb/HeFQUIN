package se.liu.ida.hefquin.federation.access.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntry;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CachePolicies;
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
import se.liu.ida.hefquin.federation.access.impl.cache.CardinalityCacheKey;
import se.liu.ida.hefquin.federation.access.impl.cache.PersistableCardinalityCacheImpl;

/**
 * A FederationAccessManager implementation that incorporates persistent disk
 * caching of SPARQL cardinality requests.
 * 
 * TODO: The implementation uses a simple serialization/deserialization
 * strategy, where the file is stored to disk by writing the full map to disk
 * for each update. This approach is not optimized for this task but is simply
 * intended as a proof of concept. A future implementation should support
 * standard cache configuration policys, such as time-based eviction (time to
 * live), but should leverage an optimized persistence strategy, preferably
 * leveraging a libary.
 * 
 * Note: Most of the classes/interfaces involved (e.g., DataRetrievalResponse,
 * CardinalityResponse etc.) do not support serialization.
 */
public class FederationAccessManagerWithPersistedDiskCache extends FederationAccessManagerWithCache
{
	protected final PersistableCardinalityCacheImpl<CardinalityCacheKey> cardinalityCache;

	public FederationAccessManagerWithPersistedDiskCache( final FederationAccessManager fedAccMan,
	                                                      final int cacheCapacity,
	                                                      final CachePolicies<Key, CompletableFuture<? extends DataRetrievalResponse<?>>, ? extends CacheEntry<CompletableFuture<? extends DataRetrievalResponse<?>>>> cachePolicies )
	{
		super( fedAccMan, cacheCapacity, cachePolicies );
		cardinalityCache = new PersistableCardinalityCacheImpl<>();
	}

	public FederationAccessManagerWithPersistedDiskCache( final FederationAccessManager fedAccMan,
	                                                      final int cacheCapacity ) 
	{
		this( fedAccMan, cacheCapacity, new MyDefaultCachePolicies() );
	}

	/**
	 * Creates a {@link FederationAccessManagerWithPersistedDiskCache} with a default configuration.
	 */
	public FederationAccessManagerWithPersistedDiskCache( final ExecutorService execService ) 
	{
		this( new AsyncFederationAccessManagerImpl( execService ), 100, new MyDefaultCachePolicies() );
	}
	
	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest( final SPARQLRequest req,
	                                                                       final SPARQLEndpoint fm )
			throws FederationAccessException
	{
		final CardinalityCacheKey key = new CardinalityCacheKey( req, fm );
		final CompletableFuture<CardinalityResponse> cachedResponse = cardinalityCache.get( key );
		if ( cachedResponse != null ) {
			cacheHitsSPARQLCardinality++;
			return cachedResponse;
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest( req, fm );
		cardinalityCache.put( key, newResponse );
		newResponse.thenRun( () -> cardinalityCache.save() );
		return newResponse;
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest( final TPFRequest req,
	                                                                       final TPFServer fm )
			throws FederationAccessException
	{
		final CardinalityCacheKey key = new CardinalityCacheKey( req, fm );
		final CompletableFuture<CardinalityResponse> cachedResponse = cardinalityCache.get( key );
		if ( cachedResponse != null ) {
			cacheHitsTPFCardinality++;
			return cachedResponse;
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest( req, fm );
		cardinalityCache.put( key, newResponse );
		newResponse.thenRun( () -> cardinalityCache.save() );
		return newResponse;
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest( final TPFRequest req,
	                                                                       final BRTPFServer fm )
			throws FederationAccessException
	{
		final CardinalityCacheKey key = new CardinalityCacheKey( req, fm );
		final CompletableFuture<CardinalityResponse> cachedResponse = cardinalityCache.get( key );
		if ( cachedResponse != null ) {
			cacheHitsTPFCardinality++;
			return cachedResponse;
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest( req, fm );
		cardinalityCache.put( key, newResponse );
		newResponse.thenRun( () -> cardinalityCache.save() );
		return newResponse;
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest( final BRTPFRequest req,
	                                                                       final BRTPFServer fm )
			throws FederationAccessException
	{
		final CardinalityCacheKey key = new CardinalityCacheKey( req, fm );
		final CompletableFuture<CardinalityResponse> cachedResponse = cardinalityCache.get( key );
		if ( cachedResponse != null ) {
			cacheHitsBRTPFCardinality++;
			return cachedResponse;
		}
		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest( req, fm );
		cardinalityCache.put( key, newResponse );
		newResponse.thenRun( () -> cardinalityCache.save() );
		return newResponse;
	}

	/**
	 * Clears the persisted cardinality cache map.
	 */
	public void clearCardinalityCache(){
		cardinalityCache.clear();
		cardinalityCache.save();
	}
}
