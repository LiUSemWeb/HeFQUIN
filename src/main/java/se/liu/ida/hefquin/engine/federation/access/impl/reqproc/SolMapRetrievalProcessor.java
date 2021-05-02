package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapRetrievalInterface;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;

public interface SolMapRetrievalProcessor<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
          extends RequestProcessor<ReqType,MemberType>
{
	/**
	 * Assumes that fm has a {@link SolMapRetrievalInterface}.
	 */
	@Override
	SolMapsResponse performRequest( ReqType req, MemberType fm );
}
