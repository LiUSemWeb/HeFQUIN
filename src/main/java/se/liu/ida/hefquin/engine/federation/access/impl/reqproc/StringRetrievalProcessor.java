package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.StringRetrievalResponse;

public interface StringRetrievalProcessor<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
        extends RequestProcessor<ReqType,MemberType>{
    @Override
    StringRetrievalResponse performRequest(ReqType req, MemberType fm );
}
