package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;

public interface RequestProcessor<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
{
	DataRetrievalResponse<?> performRequest( ReqType req, MemberType fm ) throws FederationAccessException;
}
