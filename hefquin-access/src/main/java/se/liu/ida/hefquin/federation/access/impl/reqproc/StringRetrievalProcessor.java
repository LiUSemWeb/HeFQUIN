package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.StringResponse;

public interface StringRetrievalProcessor<ReqType extends DataRetrievalRequest,
                                          MemberType extends FederationMember>
		extends RequestProcessor<ReqType,MemberType>
{
    @Override
    StringResponse performRequest(ReqType req, MemberType fm ) throws FederationAccessException;
}
