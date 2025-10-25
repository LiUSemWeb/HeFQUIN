package se.liu.ida.hefquin.federation.access.impl;

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
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.FederationAccessStats;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.TPFRequest;

public class FederationAccessManagerWithCache implements FederationAccessManager
{
	protected final FederationAccessManager fedAccMan;
	protected final Map<TriplePattern, CompletableFuture<CardinalityResponse>> cacheMap = new HashMap<>();
	protected final Cache<Key, CompletableFuture<? extends DataRetrievalResponse<?>>> cache;

	// stats
	protected long cacheRequestsSPARQL  = 0L;
	protected long cacheRequestsTPF     = 0L;
	protected long cacheRequestsBRTPF   = 0L;
	protected long cacheRequestsOther   = 0L;
	protected long cacheHitsSPARQL  = 0L;
	protected long cacheHitsTPF     = 0L;
	protected long cacheHitsBRTPF   = 0L;
	protected long cacheHitsOther   = 0L;
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


	@Override
	public < ReqType extends DataRetrievalRequest,
	         RespType extends DataRetrievalResponse<?>,
	         MemberType extends FederationMember >
	CompletableFuture<RespType> issueRequest( final ReqType req,
	                                          final MemberType fm )
			throws FederationAccessException
	{
		// update the statistics
		if ( req instanceof SPARQLRequest )
			cacheRequestsSPARQL++;
		else if ( req instanceof TPFRequest )
			cacheRequestsTPF++;
		else if ( req instanceof BRTPFRequest )
			cacheRequestsBRTPF++;
		else
			cacheRequestsOther++;

		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse<?>> cachedResponse = cache.get(key);
		if ( cachedResponse != null ) {
			// update the statistics
			if ( req instanceof SPARQLRequest )
				cacheHitsSPARQL++;
			else if ( req instanceof TPFRequest )
				cacheHitsTPF++;
			else if ( req instanceof BRTPFRequest )
				cacheHitsBRTPF++;
			else
				cacheHitsOther++;

			@SuppressWarnings("unchecked")
			final CompletableFuture<RespType> cachedResponse2 = (CompletableFuture<RespType>) cachedResponse;
			return cachedResponse2;
		}

		final CompletableFuture<RespType> newResponse = fedAccMan.issueRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(final SPARQLRequest req, final SPARQLEndpoint fm)
			throws FederationAccessException 
	{
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse<?>> cachedResponse = cache.get(key);
		if ( cachedResponse != null ) {
			cacheHitsSPARQLCardinality++;
			@SuppressWarnings("unchecked")
			final CompletableFuture<CardinalityResponse> cachedResponse2 = (CompletableFuture<CardinalityResponse>) cachedResponse;
			return cachedResponse2;
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(final TPFRequest req, final TPFServer fm)
			throws FederationAccessException 
	{
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse<?>> cachedResponse = cache.get(key);
		if ( cachedResponse != null ) {
			cacheHitsTPFCardinality++;
			@SuppressWarnings("unchecked")
			final CompletableFuture<CardinalityResponse> cachedResponse2 = (CompletableFuture<CardinalityResponse>) cachedResponse;
			return cachedResponse2;
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(final TPFRequest req, final BRTPFServer fm)
			throws FederationAccessException 
	{
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse<?>> cachedResponse = cache.get(key);
		if ( cachedResponse != null ) {
			cacheHitsTPFCardinality++;
			@SuppressWarnings("unchecked")
			final CompletableFuture<CardinalityResponse> cachedResponse2 = (CompletableFuture<CardinalityResponse>) cachedResponse;
			return cachedResponse2;
		}

		final CompletableFuture<CardinalityResponse> newResponse = fedAccMan.issueCardinalityRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(final BRTPFRequest req, final BRTPFServer fm)
			throws FederationAccessException 
	{
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse<?>> cachedResponse = cache.get(key);
		if ( cachedResponse != null ) {
			cacheHitsBRTPFCardinality++;
			@SuppressWarnings("unchecked")
			final CompletableFuture<CardinalityResponse> cachedResponse2 = (CompletableFuture<CardinalityResponse>) cachedResponse;
			return cachedResponse2;
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
		cacheRequestsOther   = 0L;

		cacheHitsSPARQL  = 0L;
		cacheHitsTPF     = 0L;
		cacheHitsBRTPF   = 0L;
		cacheHitsOther   = 0L;
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
		stats.put("numberOfOtherRequestsIssuedAtCache",  Long.valueOf(cacheRequestsOther));

		final long overallNumberOfRequestsIssuedAtCache = cacheRequestsSPARQL
		                                                + cacheRequestsTPF
		                                                + cacheRequestsBRTPF
		                                                + cacheRequestsOther;
		stats.put("overallNumberOfRequestsIssuedAtCache", Long.valueOf(overallNumberOfRequestsIssuedAtCache));

		stats.put("numberOfCacheHitsSPARQL", Long.valueOf(cacheHitsSPARQL));
		stats.put("numberOfCacheHitsTPF",    Long.valueOf(cacheHitsTPF));
		stats.put("numberOfCacheHitsBRTPF",  Long.valueOf(cacheHitsBRTPF));
		stats.put("numberOfCacheHitsOther",  Long.valueOf(cacheHitsOther));

		final long overallNumberOfCacheHits = cacheHitsSPARQL
		                                    + cacheHitsTPF
		                                    + cacheHitsBRTPF
		                                    + cacheHitsOther;
		stats.put("overallNumberOfCacheHits", Long.valueOf(overallNumberOfCacheHits));

		final double cacheHitRateSPARQL = ( (double) cacheHitsSPARQL / cacheRequestsSPARQL );
		final double cacheHitRateTPF    = ( (double) cacheHitsTPF    / cacheRequestsTPF );
		final double cacheHitRateBRTPF  = ( (double) cacheHitsBRTPF  / cacheRequestsBRTPF );
		final double cacheHitRateOther  = ( (double) cacheHitsOther  / cacheRequestsOther );
		stats.put("cacheHitRateSPARQL", Double.valueOf(cacheHitRateSPARQL));
		stats.put("cacheHitRateTPF",    Double.valueOf(cacheHitRateTPF));
		stats.put("cacheHitRateBRTPF",  Double.valueOf(cacheHitRateBRTPF));
		stats.put("cacheHitRateOther",  Double.valueOf(cacheHitRateOther));

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
