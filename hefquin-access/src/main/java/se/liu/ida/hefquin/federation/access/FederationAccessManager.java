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
	  RespType extends DataRetrievalResponse<?>,
	  MemberType extends FederationMember >
	CompletableFuture<CardinalityResponse> issueCardinalityRequest( ReqType req, MemberType fm )
			throws FederationAccessException;

	default < ReqType extends DataRetrievalRequest,
	          RespType extends DataRetrievalResponse<?>,
	          MemberType extends FederationMember >
	CompletableFuture<RespType> issueRequest( ReqType req, MemberType fm, boolean ignoreCache )
			throws FederationAccessException
	{
		return issueRequest(req, fm);
	};

	default < ReqType extends DataRetrievalRequest,
	          RespType extends DataRetrievalResponse<?>,
	          MemberType extends FederationMember >
	CompletableFuture<CardinalityResponse> issueCardinalityRequest( ReqType req,
	                                                                MemberType fm,
	                                                                boolean ignoreCardinalityCache )
			throws FederationAccessException
	{
		return issueCardinalityRequest(req, fm);
	}

	@Override
	FederationAccessStats getStats();

	/**
	 * Shuts down all thread pools associated with this federation access manager.
	 */
	void shutdown();
}
