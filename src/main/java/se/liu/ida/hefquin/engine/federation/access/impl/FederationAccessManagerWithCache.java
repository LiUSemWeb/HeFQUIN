package se.liu.ida.hefquin.engine.federation.access.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.datastructures.Cache;
import se.liu.ida.hefquin.engine.datastructures.impl.cache.CacheEntry;
import se.liu.ida.hefquin.engine.datastructures.impl.cache.CacheEntryBase;
import se.liu.ida.hefquin.engine.datastructures.impl.cache.CacheEntryBaseFactory;
import se.liu.ida.hefquin.engine.datastructures.impl.cache.CacheEntryFactory;
import se.liu.ida.hefquin.engine.datastructures.impl.cache.CacheInvalidationPolicy;
import se.liu.ida.hefquin.engine.datastructures.impl.cache.CacheInvalidationPolicyAlwaysValid;
import se.liu.ida.hefquin.engine.datastructures.impl.cache.CachePolicies;
import se.liu.ida.hefquin.engine.datastructures.impl.cache.CacheReplacementPolicy;
import se.liu.ida.hefquin.engine.datastructures.impl.cache.CacheReplacementPolicyFactory;
import se.liu.ida.hefquin.engine.datastructures.impl.cache.CacheReplacementPolicyLRU;
import se.liu.ida.hefquin.engine.datastructures.impl.cache.GenericCacheImpl;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.utils.Pair;

public class FederationAccessManagerWithCache implements FederationAccessManager
{
	protected final FederationAccessManager fedAccMan;
	protected final Map<TriplePattern, CompletableFuture<CardinalityResponse>> cacheMap = new HashMap<>();
	protected final Cache<Key, CompletableFuture<? extends DataRetrievalResponse>> cache;

	public FederationAccessManagerWithCache( final FederationAccessManager fedAccMan,
	                                         final int cacheCapacity,
	                                         final CachePolicies<Key, CompletableFuture<? extends DataRetrievalResponse>, ? extends CacheEntry<CompletableFuture<? extends DataRetrievalResponse>>> cachePolicies ) 
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


	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<SolMapsResponse> issueRequest(final SPARQLRequest req, final SPARQLEndpoint fm)
			throws FederationAccessException 
	{
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse> cachedResponse = cache.get(key);
		if ( cachedResponse != null )
			return (CompletableFuture<SolMapsResponse>) cachedResponse;

		final CompletableFuture<SolMapsResponse> newResponse = fedAccMan.issueRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<TPFResponse> issueRequest(final TPFRequest req, final TPFServer fm) 
			throws FederationAccessException 
	{
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse> cachedResponse = cache.get(key);
		if ( cachedResponse != null )
			return (CompletableFuture<TPFResponse>) cachedResponse;

		final CompletableFuture<TPFResponse> newResponse = fedAccMan.issueRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<TPFResponse> issueRequest(final TPFRequest req, final BRTPFServer fm)
			throws FederationAccessException 
	{
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse> cachedResponse = cache.get(key);
		if ( cachedResponse != null )
			return (CompletableFuture<TPFResponse>) cachedResponse;

		final CompletableFuture<TPFResponse> newResponse = fedAccMan.issueRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<TPFResponse> issueRequest(final BRTPFRequest req, final BRTPFServer fm)
			throws FederationAccessException 
	{
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse> cachedResponse = cache.get(key);
		if ( cachedResponse != null )
			return (CompletableFuture<TPFResponse>) cachedResponse;

		final CompletableFuture<TPFResponse> newResponse = fedAccMan.issueRequest(req, fm);
		cache.put(key, newResponse);
		return newResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<RecordsResponse> issueRequest(final Neo4jRequest req, final Neo4jServer fm)
			throws FederationAccessException 
	{
		final Key key = new Key(req, fm);
		final CompletableFuture<? extends DataRetrievalResponse> cachedResponse = cache.get(key);
		if ( cachedResponse != null )
			return (CompletableFuture<RecordsResponse>) cachedResponse;

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
		final CompletableFuture<? extends DataRetrievalResponse> cachedResponse = cache.get(key);
		if ( cachedResponse != null )
			return (CompletableFuture<CardinalityResponse>) cachedResponse;

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
		final CompletableFuture<? extends DataRetrievalResponse> cachedResponse = cache.get(key);
		if ( cachedResponse != null )
			return (CompletableFuture<CardinalityResponse>) cachedResponse;

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
		final CompletableFuture<? extends DataRetrievalResponse> cachedResponse = cache.get(key);
		if ( cachedResponse != null )
			return (CompletableFuture<CardinalityResponse>) cachedResponse;

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
		final CompletableFuture<? extends DataRetrievalResponse> cachedResponse = cache.get(key);
		if ( cachedResponse != null )
			return (CompletableFuture<CardinalityResponse>) cachedResponse;

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
				                         CompletableFuture<? extends DataRetrievalResponse>,
				                         CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse>>>
	{
		final CacheEntryFactory<CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse>>,
		                        CompletableFuture<? extends DataRetrievalResponse>
		                       > cef = new CacheEntryBaseFactory<>();

		final CacheReplacementPolicyFactory<Key,
		                                    CompletableFuture<? extends DataRetrievalResponse>,
		                                    CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse>>
		                                   > crpf = new CacheReplacementPolicyFactory<>() {
			@Override
			public CacheReplacementPolicy<Key, CompletableFuture<? extends DataRetrievalResponse>, CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse>>> create() {
				return new CacheReplacementPolicyLRU<>();
			}
		};

		final CacheInvalidationPolicy<CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse>>,
		                              CompletableFuture<? extends DataRetrievalResponse>
		                             > cip = new CacheInvalidationPolicyAlwaysValid<>();

		@Override
		public CacheEntryFactory<CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse>>, CompletableFuture<? extends DataRetrievalResponse>> getEntryFactory() {
			return cef;
		}

		@Override
		public CacheReplacementPolicyFactory<Key, CompletableFuture<? extends DataRetrievalResponse>, CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse>>> getReplacementPolicyFactory() {
			return crpf;
		}

		@Override
		public CacheInvalidationPolicy<CacheEntryBase<CompletableFuture<? extends DataRetrievalResponse>>, CompletableFuture<? extends DataRetrievalResponse>> getInvalidationPolicy() {
			return cip;
		}

	} // end of MyDefaultCachePolicies

}
