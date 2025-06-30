package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.federation.access.SPARQLEndpointInterface;

public interface SPARQLEndpoint extends FederationMember
{
	@Override
	SPARQLEndpointInterface getInterface();
}
