package se.liu.ida.hefquin.federation.access;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.base.utils.StatsProvider;
import se.liu.ida.hefquin.federation.FederationMember;

public interface FederationAccessManager extends StatsProvider
{
	< ReqType extends DataRetrievalRequest,
	  RespType extends DataRetrievalResponse<?>,
	  MemberType extends FederationMember >
	CompletableFuture<RespType> issueRequest( ReqType req, MemberType fm )
			throws FederationAccessException;

	< ReqType extends DataRetrievalRequest,
	  MemberType extends FederationMember >
	CompletableFuture<CardinalityResponse> issueCardinalityRequest( final ReqType req, final MemberType fm )
			throws FederationAccessException;

	@Override
	FederationAccessStats getStats();

	/**
	 * Shuts down all thread pools associated with this federation access manager.
	 */
	void shutdown();
}
