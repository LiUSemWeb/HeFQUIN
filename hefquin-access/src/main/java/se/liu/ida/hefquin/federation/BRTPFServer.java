package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.federation.access.BRTPFInterface;

public interface BRTPFServer extends FederationMember
{
	@Override
	BRTPFInterface getInterface();
}
