package se.liu.ida.hefquin.engine.federation;

import se.liu.ida.hefquin.engine.federation.access.Neo4jInterface;

public interface Neo4jServer extends FederationMember{
    @Override
    Neo4jInterface getInterface();
}
