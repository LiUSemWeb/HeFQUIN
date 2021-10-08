package se.liu.ida.hefquin.engine.federation.access.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.StringResponse;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.query.TriplePattern;

public class FederationAccessManagerWithCache implements FederationAccessManager 
{
	protected final FederationAccessManager fedAccMan;
	protected final Map<TriplePattern, CompletableFuture<CardinalityResponse>> cacheMap = new HashMap<>();
	
	public FederationAccessManagerWithCache(final FederationAccessManager fedAccMan) {
		assert fedAccMan != null;
		this.fedAccMan = fedAccMan;
		this.cacheMap = new HashMap<TriplePattern, CompletableFuture<CardinalityResponse>>();
	}
	
	protected void addResultToCache(final TriplePattern tp, final CompletableFuture<CardinalityResponse> response) {
		cacheMap.put(tp, response);
	}
	
	@Override
	public CompletableFuture<SolMapsResponse> issueRequest(final SPARQLRequest req, final SPARQLEndpoint fm)
			throws FederationAccessException {
		return fedAccMan.issueRequest(req, fm);
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest(TPFRequest req, TPFServer fm) throws FederationAccessException {
		return fedAccMan.issueRequest(req, fm);
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest(TPFRequest req, BRTPFServer fm)
			throws FederationAccessException {
		return fedAccMan.issueRequest(req, fm);
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest(BRTPFRequest req, BRTPFServer fm)
			throws FederationAccessException {
		return fedAccMan.issueRequest(req, fm);
	}

	@Override
	public CompletableFuture<StringResponse> issueRequest(Neo4jRequest req, Neo4jServer fm)
			throws FederationAccessException {
		return fedAccMan.issueRequest(req, fm);
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(SPARQLRequest req, SPARQLEndpoint fm)
			throws FederationAccessException {
		return fedAccMan.issueCardinalityRequest(req, fm);
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(final TPFRequest req, final TPFServer fm)
			throws FederationAccessException {
		final CompletableFuture<CardinalityResponse> cacheResponse = cacheMap.get(req.getQueryPattern());
		if (cacheResponse == null) {
			final CompletableFuture<CardinalityResponse> newFutureResponse = fedAccMan.issueCardinalityRequest(req, fm);
			addResultToCache(req.getQueryPattern(), newFutureResponse);
			return newFutureResponse;
		}
		else {
			return cacheResponse;
		}
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(final TPFRequest req, final BRTPFServer fm)
			throws FederationAccessException {
		return fedAccMan.issueCardinalityRequest(req, fm);
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(final BRTPFRequest req, final BRTPFServer fm)
			throws FederationAccessException {
		return fedAccMan.issueCardinalityRequest(req, fm);
	}
}
