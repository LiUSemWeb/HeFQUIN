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
	private FederationAccessManager fedAccMan;
	private Map<TriplePattern, CompletableFuture<CardinalityResponse>> cacheMap;
	//private Map<TriplePattern, Integer> cacheMap;

	public FederationAccessManagerWithCache() {
		System.out.println("Inside normal constructor");
	}
	
	public FederationAccessManagerWithCache(FederationAccessManager fedAccMan) {
		this.fedAccMan = fedAccMan;
		//this.cacheMap = new HashMap<TriplePattern, Integer>();
		this.cacheMap = new HashMap<TriplePattern, CompletableFuture<CardinalityResponse>>();
		System.out.println("Inside constructor creating fedAccMan and cachemap");
	}
	
	public void addResultToCache(TriplePattern tp, CompletableFuture<CardinalityResponse> response) {
		cacheMap.put(tp, response);
	}
	
	/*public void addResultToCache(TriplePattern tp, Integer response) {
		cacheMap.put(tp, response);
	}*/

	@Override
	public CompletableFuture<SolMapsResponse> issueRequest(SPARQLRequest req, SPARQLEndpoint fm)
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

	// TODO Add caching for this request
	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(TPFRequest req, TPFServer fm)
			throws FederationAccessException {
		System.out.println(cacheMap.values());
		//Integer cacheResponse = cacheMap.get(req.getQueryPattern());
		CompletableFuture<CardinalityResponse> cacheResponse = cacheMap.get(req.getQueryPattern());
		if (cacheResponse == null) {
			System.out.println("Did not find in cache - running request");
			return fedAccMan.issueCardinalityRequest(req, fm).thenApply((response) -> addResultToCache(req.getQueryPattern(), response));
		}
		else {
			System.out.println("Found, returning from cache!");
			return cacheResponse;
		}
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(TPFRequest req, BRTPFServer fm)
			throws FederationAccessException {
		return fedAccMan.issueCardinalityRequest(req, fm);
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(BRTPFRequest req, BRTPFServer fm)
			throws FederationAccessException {
		return fedAccMan.issueCardinalityRequest(req, fm);
	}
}
