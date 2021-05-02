package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplesResponse;
import se.liu.ida.hefquin.engine.federation.access.TriplesRetrievalInterface;

public interface TriplesRetrievalProcessor<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
            extends RequestProcessor<ReqType,MemberType>
{
	/**
	 * Assumes that fm has a {@link TriplesRetrievalInterface}.
	 */
	@Override
	TriplesResponse performRequest( ReqType req, MemberType fm );
}
