package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Set;

public interface WhereCondition {
    Set<CypherVar> getVars();
}
