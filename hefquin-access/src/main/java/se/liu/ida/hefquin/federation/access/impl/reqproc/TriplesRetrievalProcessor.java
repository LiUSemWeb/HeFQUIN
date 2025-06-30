package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.TriplesResponse;
import se.liu.ida.hefquin.federation.access.TriplesRetrievalInterface;

public interface TriplesRetrievalProcessor<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
            extends RequestProcessor<ReqType,MemberType>
{
	/**
	 * Assumes that fm has a {@link TriplesRetrievalInterface}.
	 */
	@Override
	TriplesResponse performRequest( ReqType req, MemberType fm ) throws FederationAccessException;
}
