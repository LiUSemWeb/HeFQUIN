package se.liu.ida.hefquin.engine.federation.access.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

public class FederationAccessManagerWithCache implements FederationAccessManager
{

	public FederationAccessManagerWithCache() 
	{
		CompletableFuture<CardinalityResponse> cachedResponse = new CompletableFuture<CardinalityResponse>();
		TPFRequest cachedReq;
	}	


	@Override
	public CompletableFuture<SolMapsResponse> issueRequest(SPARQLRequest req, SPARQLEndpoint fm)
			throws FederationAccessException 
	{
		return null;
	}


	@Override
	public CompletableFuture<TPFResponse> issueRequest(TPFRequest req, TPFServer fm) throws FederationAccessException 
	{
		return null;
	}


	@Override
	public CompletableFuture<TPFResponse> issueRequest(TPFRequest req, BRTPFServer fm)
			throws FederationAccessException 
	{
		return null;
	}


	@Override
	public CompletableFuture<TPFResponse> issueRequest(BRTPFRequest req, BRTPFServer fm)
			throws FederationAccessException 
	{
		return null;
	}


	@Override
	public CompletableFuture<StringResponse> issueRequest(Neo4jRequest req, Neo4jServer fm)
			throws FederationAccessException 
	{
		return null;
	}


	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(SPARQLRequest req, SPARQLEndpoint fm)
			throws FederationAccessException 
	{
		return null;
	}
	
	// The function to add caching to
	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(TPFRequest req, TPFServer fm)
			throws FederationAccessException 
	{
		System.out.println("The request sent: " + req.getQueryPattern());
		return null;
	}


	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(TPFRequest req, BRTPFServer fm)
			throws FederationAccessException 
	{
		return null;
	}


	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(BRTPFRequest req, BRTPFServer fm)
			throws FederationAccessException 
	{
		return null;
	}
	


	public static void main(String[] args) throws InterruptedException, ExecutionException, FederationAccessException 
	{
		FederationAccessManager fedMan = new FederationAccessManagerWithCache();
		// Run some query
		// If cache has answer - return from cache
		// Else - Complete query and save new answer in cache
		System.out.println("Run some test for cache?");
	}

}
