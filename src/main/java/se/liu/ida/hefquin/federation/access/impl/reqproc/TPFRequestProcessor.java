package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;

public interface TPFRequestProcessor extends TriplesRetrievalProcessor<TriplePatternRequest,TPFServer>
{
	TriplesResponse performRequest( TriplePatternRequest req, BRTPFServer fm );
}
