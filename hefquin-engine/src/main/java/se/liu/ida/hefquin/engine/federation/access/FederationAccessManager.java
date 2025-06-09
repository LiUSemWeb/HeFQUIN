package se.liu.ida.hefquin.engine.federation.access;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLQuery;
import se.liu.ida.hefquin.base.utils.StatsProvider;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;

public interface FederationAccessManager extends StatsProvider
{
	CompletableFuture<SolMapsResponse> issueRequest( SPARQLRequest req, SPARQLEndpoint fm ) throws FederationAccessException;

	CompletableFuture<TPFResponse> issueRequest( TPFRequest req, TPFServer fm ) throws FederationAccessException;

	CompletableFuture<TPFResponse> issueRequest( TPFRequest req, BRTPFServer fm ) throws FederationAccessException;

	CompletableFuture<TPFResponse> issueRequest( BRTPFRequest req, BRTPFServer fm ) throws FederationAccessException;

	CompletableFuture<RecordsResponse> issueRequest( Neo4jRequest req, Neo4jServer fm ) throws FederationAccessException;

	/**
	 * Requests the cardinality of the result of the given request.
	 *
	 * Assumes that the given request contains a {@link SPARQLGraphPattern}
	 * rather than a full {@link SPARQLQuery}. If it does not, then this
	 * method throws an {@link IllegalArgumentException}.
	 */
	CompletableFuture<CardinalityResponse> issueCardinalityRequest( SPARQLRequest req, SPARQLEndpoint fm ) throws FederationAccessException;

	CompletableFuture<CardinalityResponse> issueCardinalityRequest( TPFRequest req, TPFServer fm ) throws FederationAccessException;

	CompletableFuture<CardinalityResponse> issueCardinalityRequest( TPFRequest req, BRTPFServer fm ) throws FederationAccessException;

	CompletableFuture<CardinalityResponse> issueCardinalityRequest( BRTPFRequest req, BRTPFServer fm ) throws FederationAccessException;

	@Override
	FederationAccessStats getStats();

	/**
	 * Shuts down all thread pools associated with this federation access manager.
	 */
	void shutdown();
}
