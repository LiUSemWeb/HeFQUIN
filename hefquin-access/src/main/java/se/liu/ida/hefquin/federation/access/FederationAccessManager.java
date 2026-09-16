package se.liu.ida.hefquin.federation.access;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.base.utils.StatsProvider;
import se.liu.ida.hefquin.federation.FederationMember;

public interface FederationAccessManager extends StatsProvider
{
	< ReqType extends DataRetrievalRequest,
	  RespType extends DataRetrievalResponse<?>,
	  MemberType extends FederationMember >
	CompletableFuture<RespType> issueRequest( ReqType req, MemberType fm, boolean ignoreRetrievalCache );

	< ReqType extends DataRetrievalRequest,
	  RespType extends DataRetrievalResponse<?>,
	  MemberType extends FederationMember >
	CompletableFuture<CardinalityResponse> issueCardinalityRequest( ReqType req,
	                                                                MemberType fm,
	                                                                boolean ignoreCardinalityCache );

	@Override
	FederationAccessStats getStats();

	/**
	 * Shuts down all thread pools associated with this federation access manager.
	 */
	void shutdown();
}
