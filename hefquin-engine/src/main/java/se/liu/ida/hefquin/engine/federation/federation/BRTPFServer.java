package se.liu.ida.hefquin.engine.federation;

import se.liu.ida.hefquin.engine.federation.access.BRTPFInterface;

public interface BRTPFServer extends FederationMember
{
	@Override
	BRTPFInterface getInterface();
}
