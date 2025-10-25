package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.RecordsResponse;
import se.liu.ida.hefquin.federation.access.impl.RequestProcessor;

public interface RecordsRetrievalProcessor <ReqType extends DataRetrievalRequest,
                                            MemberType extends FederationMember>
	extends RequestProcessor<ReqType,RecordsResponse,MemberType>
{
	@Override
	default boolean isSupportedResponseType( final Class<? extends DataRetrievalResponse<?>> t ) {
		return RecordsResponse.class.isAssignableFrom(t);
	}
}
