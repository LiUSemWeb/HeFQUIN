package se.liu.ida.hefquin.engine.query;

import se.liu.ida.hefquin.engine.query.cypher.CypherVar;

import java.util.Set;

public interface CypherQuery extends Query {
    String toString();
    Set<CypherVar> getMatchVars();
}
