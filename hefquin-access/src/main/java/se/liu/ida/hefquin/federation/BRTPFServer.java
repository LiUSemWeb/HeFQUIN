package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.federation.access.BRTPFRequest;

public interface BRTPFServer extends TPFServer
{
	String createRequestURL( BRTPFRequest req );
}
