package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.federation.access.Neo4jInterface;

public interface Neo4jServer extends FederationMember{
    @Override
    Neo4jInterface getInterface();
}
