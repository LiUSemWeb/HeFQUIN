package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.RecordsResponse;
import se.liu.ida.hefquin.engine.federation.access.RecordsRetrievalInterface;

public interface RecordsRetrievalProcessor <ReqType extends DataRetrievalRequest,
                                            MemberType extends FederationMember>
        extends RequestProcessor<ReqType,MemberType>
{
    /**
     * Assumes that fm has a {@link RecordsRetrievalInterface}.
     */
    @Override
    RecordsResponse performRequest( ReqType req, MemberType fm ) throws FederationAccessException;
}
