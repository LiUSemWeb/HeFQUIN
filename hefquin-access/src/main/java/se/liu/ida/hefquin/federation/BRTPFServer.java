package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.federation.access.BRTPFInterface;

public interface BRTPFServer extends TPFServer
{
	@Override
	BRTPFInterface getInterface();
}
