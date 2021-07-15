package se.liu.ida.hefquin.engine.query;

import java.util.Set;

public interface CypherQuery {
    String toString();

    Set<String> getMatches();
    Set<String> getConditions();
    Set<String> getReturnExprs();
    Set<CypherQuery> getUnion();

    void addMatchClause(final String match);
    void addConditionConjunction(final String cond);
    void addReturnClause(final String ret);
    void addQueryToUnion(final CypherQuery q);

    boolean isMatchQuery();
    boolean isUnionQuery();
}
