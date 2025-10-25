package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.impl.RequestProcessor;

public interface SolMapRetrievalProcessor<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
          extends RequestProcessor<ReqType,SolMapsResponse,MemberType>
{
	@Override
	default boolean isSupportedResponseType( final Class<? extends DataRetrievalResponse<?>> t ) {
		return SolMapsResponse.class.isAssignableFrom(t);
	}
}
