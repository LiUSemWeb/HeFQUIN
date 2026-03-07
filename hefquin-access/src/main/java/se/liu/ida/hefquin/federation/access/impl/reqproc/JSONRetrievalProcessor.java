package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.JSONResponse;
import se.liu.ida.hefquin.federation.access.impl.RequestProcessor;

public interface JSONRetrievalProcessor<ReqType extends DataRetrievalRequest,
                                        MemberType extends FederationMember>
         extends RequestProcessor<ReqType,JSONResponse,MemberType>
{
	@Override
	default boolean isSupportedResponseType( final Class<? extends DataRetrievalResponse<?>> t ) {
		return JSONResponse.class.isAssignableFrom(t);
	}
}
