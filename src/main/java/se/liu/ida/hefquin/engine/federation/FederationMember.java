package se.liu.ida.hefquin.engine.federation;

import se.liu.ida.hefquin.engine.federation.access.DataRetrievalInterface;

public interface FederationMember
{
	DataRetrievalInterface getInterface();
}
