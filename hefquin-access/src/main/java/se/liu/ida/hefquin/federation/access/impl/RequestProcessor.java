package se.liu.ida.hefquin.federation.access.impl;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;

/**
 * This interface captures any kind of request processor.
 *
 * @param <ReqType> - the type of requests that this request
 *                    processor is capable of processing
 * @param <RespType> - the type of responses returned by this
 *                     request processor
 * @param <MemberType> - the type of federation members that
 *                       this request processor can interact
 *                       with
 */
public interface RequestProcessor<ReqType extends DataRetrievalRequest,
                                  RespType extends DataRetrievalResponse<?>,
                                  MemberType extends FederationMember>
{
	boolean isSupportedRequestType( Class<? extends DataRetrievalRequest> t );
	boolean isSupportedResponseType( Class<? extends DataRetrievalResponse<?>> t );
	boolean isSupportedMemberType( Class<? extends FederationMember> t );

	RespType performRequest( ReqType req, MemberType fm ) throws FederationAccessException;
}
