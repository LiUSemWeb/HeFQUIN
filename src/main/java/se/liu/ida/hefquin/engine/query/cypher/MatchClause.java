package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Set;

public interface MatchClause {
    boolean isRedundantWith(final MatchClause match);

    Set<CypherVar> getVars();
}
