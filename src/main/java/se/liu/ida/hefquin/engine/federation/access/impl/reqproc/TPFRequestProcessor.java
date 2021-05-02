package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;

public interface TPFRequestProcessor extends TriplesRetrievalProcessor<TPFRequest,TPFServer>
{
	@Override
	TPFResponse performRequest( TPFRequest req, TPFServer fm );

	TPFResponse performRequest( TPFRequest req, BRTPFServer fm );
}
