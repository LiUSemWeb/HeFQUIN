package se.liu.ida.hefquin.engine.federation;

import se.liu.ida.hefquin.engine.federation.access.TPFInterface;

public interface TPFServer extends FederationMember
{
	@Override
	TPFInterface getInterface();
}
