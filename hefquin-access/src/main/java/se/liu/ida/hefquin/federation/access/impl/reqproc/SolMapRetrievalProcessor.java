package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.SolMapRetrievalInterface;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;

public interface SolMapRetrievalProcessor<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
          extends RequestProcessor<ReqType,MemberType>
{
	/**
	 * Assumes that fm has a {@link SolMapRetrievalInterface}.
	 */
	@Override
	SolMapsResponse performRequest( ReqType req, MemberType fm ) throws FederationAccessException;
}
