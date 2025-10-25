package se.liu.ida.hefquin.federation.access;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLQuery;
import se.liu.ida.hefquin.base.utils.StatsProvider;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.TPFServer;

public interface FederationAccessManager extends StatsProvider
{
	< ReqType extends DataRetrievalRequest,
	  RespType extends DataRetrievalResponse<?>,
	  MemberType extends FederationMember >
	CompletableFuture<RespType> issueRequest( ReqType req, MemberType fm )
			throws FederationAccessException;

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
