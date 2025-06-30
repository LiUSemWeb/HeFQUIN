package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.JSONResponse;
import se.liu.ida.hefquin.engine.federation.access.JSONRetrievalInterface;

public interface JSONRetrievalProcessor<ReqType extends DataRetrievalRequest,
                                        MemberType extends FederationMember>
         extends RequestProcessor<ReqType,MemberType>
{
    /**
     * Assumes that fm has a {@link JSONRetrievalInterface}.
     */
    @Override
    JSONResponse performRequest( ReqType req, MemberType fm ) throws FederationAccessException;

}
