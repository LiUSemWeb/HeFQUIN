package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;
import se.liu.ida.hefquin.federation.access.impl.RequestProcessor;

public interface TriplesRetrievalProcessor<ReqType extends DataRetrievalRequest,
                                           RespType extends TriplesResponse,
                                           MemberType extends FederationMember>
		extends RequestProcessor<ReqType,RespType,MemberType>
{

}
