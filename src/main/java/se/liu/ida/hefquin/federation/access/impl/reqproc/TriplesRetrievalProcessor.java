package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;

public interface TriplesRetrievalProcessor extends RequestProcessor
{
	@Override
	TriplesResponse performRequest( final DataRetrievalRequest req );
}
