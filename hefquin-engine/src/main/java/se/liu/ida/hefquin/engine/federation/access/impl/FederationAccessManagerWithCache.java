package se.liu.ida.hefquin.engine.federation.access.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.base.datastructures.Cache;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntry;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryBase;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryBaseFactory;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheEntryFactory;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheInvalidationPolicyAlwaysValid;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CachePolicies;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicy;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicyFactory;
import se.liu.ida.hefquin.base.datastructures.impl.cache.CacheReplacementPolicyLRU;
import se.liu.ida.hefquin.base.datastructures.impl.cache.GenericCacheImpl;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.*;

public class FederationAccessManagerWithCache implements FederationAccessManager
{
	protected final FederationAccessManager fedAccMan;
	protected final Map<TriplePattern, CompletableFuture<CardinalityResponse>> cacheMap = new HashMap<>();
	protected final Cache<Key, CompletableFuture<? extends DataRetrievalResponse<?>>> cache;

	// stats
	protected long cacheRequestsSPARQL  = 0L;
	protected long cacheRequestsTPF     = 0L;
	protected long cacheRequestsBRTPF   = 0L;
	protected long cacheRequestsNeo4j   = 0L;
	protected long cacheHitsSPARQL  = 0L;
	protected long cacheHitsTPF     = 0L;
	protected long cacheHitsBRTPF   = 0L;
	protected long cacheHitsNeo4j   = 0L;
	protected long cacheHitsSPARQLCardinality  = 0L;
	protected long cacheHitsTPFCardinality     = 0L;
	protected long cacheHitsBRTPFCardinality   = 0L;

	public FederationAccessManagerWithCache( final FederationAccessManager fedAccMan,
	                                         final int cacheCapacity,
	                                         final CachePolicies<Key, CompletableFuture<? extends DataRetrievalResponse<?>>, ? extends CacheEntry<CompletableFuture<? extends DataRetrievalResponse<?>>>> cachePolicies ) 
	{
		assert fedAccMan != null;
		this.fedAccMan = fedAccMan;

		cache = new GenericCacheImpl<>(cacheCapacity, cachePolicies);
	}

	public FederationAccessManagerWithCache( final FederationAccessManager fedAccMan,
	                                         final int cacheCapacity ) 
	{
		this( fedAccMan, cacheCapacity, new MyDefaultCachePolicies() );
	}

	/**
	 * Creates a {@link FederationAccessManagerWithCache} with a default configuration.
	 */
	public FederationAccessManagerWithCache( final ExecutorService execService ) 
	{
		this( new AsyncFederationAccessManagerImpl(execService),
		      100,
		      new MyDefaultCachePolicies() );
	}


	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<SolMapsResponse> issueRequest(final SPARQLRequest req, final SPARQLEndpoint fm)
			throws FederationAccessException 
	{
		cacheRequestsSPARQL++;
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse<?>> cachedResponse = cache.get(key);
		if ( cachedResponse != null ) {
			cacheHitsSPARQL++;
			return (CompletableFuture<SolMapsResponse>) cachedResponse;
		}

		final CompletableFuture<SolMapsResponse> newResponse = fedAccMan.issueRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<TPFResponse> issueRequest(final TPFRequest req, final TPFServer fm) 
			throws FederationAccessException 
	{
		cacheRequestsTPF++;
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse<?>> cachedResponse = cache.get(key);
		if ( cachedResponse != null ) {
			cacheHitsTPF++;
			return (CompletableFuture<TPFResponse>) cachedResponse;
		}

		final CompletableFuture<TPFResponse> newResponse = fedAccMan.issueRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<TPFResponse> issueRequest(final TPFRequest req, final BRTPFServer fm)
			throws FederationAccessException 
	{
		cacheRequestsTPF++;
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse<?>> cachedResponse = cache.get(key);
		if ( cachedResponse != null ) {
			cacheHitsTPF++;
			return (CompletableFuture<TPFResponse>) cachedResponse;
		}

		final CompletableFuture<TPFResponse> newResponse = fedAccMan.issueRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<TPFResponse> issueRequest(final BRTPFRequest req, final BRTPFServer fm)
			throws FederationAccessException 
	{
		cacheRequestsBRTPF++;
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse<?>> cachedResponse = cache.get(key);
		if ( cachedResponse != null ) {
			cacheHitsBRTPF++;
			return (CompletableFuture<TPFResponse>) cachedResponse;
		}

		final CompletableFuture<TPFResponse> newResponse = fedAccMan.issueRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<RecordsResponse> issueRequest(final Neo4jRequest req, final Neo4jServer fm)
			throws FederationAccessException 
	{
		cacheRequestsNeo4j++;
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse<?>> cachedResponse = cache.get(key);
		if ( cachedResponse != null ) {
			cacheHitsNeo4j++;
			return (CompletableFuture<RecordsResponse>) cachedResponse;
		}

		final CompletableFuture<RecordsResponse> newResponse = fedAccMan.issueRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(final SPARQLRequest req, final SPARQLEndpoint fm)
			throws FederationAccessException 
	{
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse<?>> cachedResponse = cache.get(key);
		if ( cachedResponse != null ) {
			cacheHitsSPARQLCardinality++;
			return (CompletableFuture<CardinalityResponse>) cachedResponse;
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(final TPFRequest req, final TPFServer fm)
			throws FederationAccessException 
	{
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse<?>> cachedResponse = cache.get(key);
		if ( cachedResponse != null ) {
			cacheHitsTPFCardinality++;
			return (CompletableFuture<CardinalityResponse>) cachedResponse;
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(final TPFRequest req, final BRTPFServer fm)
			throws FederationAccessException 
	{
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse<?>> cachedResponse = cache.get(key);
		if ( cachedResponse != null ) {
			cacheHitsTPFCardinality++;
			return (CompletableFuture<CardinalityResponse>) cachedResponse;
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(final BRTPFRequest req, final BRTPFServer fm)
			throws FederationAccessException 
	{
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse<?>> cachedResponse = cache.get(key);
		if ( cachedResponse != null ) {
			cacheHitsBRTPFCardinality++;
			return (CompletableFuture<CardinalityResponse>) cachedResponse;
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}


	protected static class Key extends Pair<DataRetrievalRequest, FederationMember> {
		public Key( final DataRetrievalRequest req, final FederationMember fm ) {
			super(req, fm);
		}
	}


	protected static class MyDefaultCachePolicies
				implements CachePolicies<Key,
				                         CompletableFuture<? extends DataRetrievalResponse<?>>,
				                         CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse<?>>>>
	{
		final CacheEntryFactory<CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse<?>>>,
		                        CompletableFuture<? extends DataRetrievalResponse<?>>
		                       > cef = new CacheEntryBaseFactory<>();

		final CacheReplacementPolicyFactory<Key,
		                                    CompletableFuture<? extends DataRetrievalResponse<?>>,
		                                    CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse<?>>>
		                                   > crpf = new CacheReplacementPolicyFactory<>() {
			@Override
			public CacheReplacementPolicy<Key, CompletableFuture<? extends DataRetrievalResponse<?>>, CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse<?>>>> create() {
				return new CacheReplacementPolicyLRU<>();
			}
		};

		final CacheInvalidationPolicy<CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse<?>>>,
		                              CompletableFuture<? extends DataRetrievalResponse<?>>
		                             > cip = new CacheInvalidationPolicyAlwaysValid<>();

		@Override
		public CacheEntryFactory<CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse<?>>>, CompletableFuture<? extends DataRetrievalResponse<?>>> getEntryFactory() {
			return cef;
		}

		@Override
		public CacheReplacementPolicyFactory<Key, CompletableFuture<? extends DataRetrievalResponse<?>>, CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse<?>>>> getReplacementPolicyFactory() {
			return crpf;
		}

		@Override
		public CacheInvalidationPolicy<CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse<?>>>, CompletableFuture<? extends DataRetrievalResponse<?>>> getInvalidationPolicy() {
			return cip;
		}

	} // end of MyDefaultCachePolicies


	@Override
	public void resetStats() {
		fedAccMan.resetStats();

		cacheRequestsSPARQL  = 0L;
		cacheRequestsTPF     = 0L;
		cacheRequestsBRTPF   = 0L;
		cacheRequestsNeo4j   = 0L;

		cacheHitsSPARQL  = 0L;
		cacheHitsTPF     = 0L;
		cacheHitsBRTPF   = 0L;
		cacheHitsNeo4j   = 0L;
	}

	@Override
	public FederationAccessStats getStats() {
		final FederationAccessStatsImpl stats = (FederationAccessStatsImpl) fedAccMan.getStats();

		stats.put("numberOfCacheHitsCardinalitySPARQL", Long.valueOf(cacheHitsSPARQLCardinality));
		stats.put("numberOfCacheHitsCardinalityTPF",    Long.valueOf(cacheHitsTPFCardinality));
		stats.put("numberOfCacheHitsCardinalityBRTPF",  Long.valueOf(cacheHitsBRTPFCardinality));

		final long overallNumberOfCacheHitsCardinality = cacheHitsSPARQLCardinality
		                                               + cacheHitsTPFCardinality
		                                               + cacheHitsBRTPFCardinality;
		stats.put("overallNumberOfCacheHitsCardinality", Long.valueOf(overallNumberOfCacheHitsCardinality));

		final double cacheHitRateCardinality = ( (double) overallNumberOfCacheHitsCardinality / (Long) stats.getEntry(FederationAccessManagerBase1.enOverallNumberOfCardRequestsIssued) );
		stats.put("cacheHitRateForCardinalityRequest", Double.valueOf(cacheHitRateCardinality));

		stats.put("numberOfSPARQLRequestsIssuedAtCache", Long.valueOf(cacheRequestsSPARQL));
		stats.put("numberOfTPFRequestsIssuedAtCache",    Long.valueOf(cacheRequestsTPF));
		stats.put("numberOfBRTPFRequestsIssuedAtCache",  Long.valueOf(cacheRequestsBRTPF));
		stats.put("numberOfNeo4jRequestsIssuedAtCache",  Long.valueOf(cacheRequestsNeo4j));

		final long overallNumberOfRequestsIssuedAtCache = cacheRequestsSPARQL
		                                                + cacheRequestsTPF
		                                                + cacheRequestsBRTPF
		                                                + cacheRequestsNeo4j;
		stats.put("overallNumberOfRequestsIssuedAtCache", Long.valueOf(overallNumberOfRequestsIssuedAtCache));

		stats.put("numberOfCacheHitsSPARQL", Long.valueOf(cacheHitsSPARQL));
		stats.put("numberOfCacheHitsTPF",    Long.valueOf(cacheHitsTPF));
		stats.put("numberOfCacheHitsBRTPF",  Long.valueOf(cacheHitsBRTPF));
		stats.put("numberOfCacheHitsNeo4j",  Long.valueOf(cacheHitsNeo4j));

		final long overallNumberOfCacheHits = cacheHitsSPARQL
		                                    + cacheHitsTPF
		                                    + cacheHitsBRTPF
		                                    + cacheHitsNeo4j;
		stats.put("overallNumberOfCacheHits", Long.valueOf(overallNumberOfCacheHits));

		final double cacheHitRateSPARQL = ( (double) cacheHitsSPARQL / cacheRequestsSPARQL );
		final double cacheHitRateTPF    = ( (double) cacheHitsTPF    / cacheRequestsTPF );
		final double cacheHitRateBRTPF  = ( (double) cacheHitsBRTPF  / cacheRequestsBRTPF );
		final double cacheHitRateNeo4j  = ( (double) cacheHitsNeo4j  / cacheRequestsNeo4j );
		stats.put("cacheHitRateSPARQL", Double.valueOf(cacheHitRateSPARQL));
		stats.put("cacheHitRateTPF",    Double.valueOf(cacheHitRateTPF));
		stats.put("cacheHitRateBRTPF",  Double.valueOf(cacheHitRateBRTPF));
		stats.put("cacheHitRateNeo4j",  Double.valueOf(cacheHitRateNeo4j));

		final double cacheHitRate = ( (double) overallNumberOfCacheHits / overallNumberOfRequestsIssuedAtCache );
		stats.put("cacheHitRate", cacheHitRate);

		return stats;
	}

	/**
	 * Shuts down all thread pools associated with this federation access manager.
	 */
	@Override
	public void shutdown() {
		fedAccMan.shutdown();
	}
}
