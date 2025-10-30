package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.StringResponse;
import se.liu.ida.hefquin.federation.access.impl.RequestProcessor;

public interface StringRetrievalProcessor<ReqType extends DataRetrievalRequest,
                                          MemberType extends FederationMember>
		extends RequestProcessor<ReqType,StringResponse,MemberType>
{
	@Override
	default boolean isSupportedResponseType( final Class<? extends DataRetrievalResponse<?>> t ) {
		return StringResponse.class.isAssignableFrom(t);
	}
}
