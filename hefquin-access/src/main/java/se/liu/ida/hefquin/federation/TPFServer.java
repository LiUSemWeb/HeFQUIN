package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.federation.access.TPFInterface;

public interface TPFServer extends FederationMember
{
	@Override
	TPFInterface getInterface();
}
