package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;

public interface RequestProcessor
{
	DataRetrievalResponse performRequest( final DataRetrievalRequest req, final FederationMember fm );
}
