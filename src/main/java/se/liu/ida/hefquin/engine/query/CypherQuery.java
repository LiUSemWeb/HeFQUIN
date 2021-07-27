package se.liu.ida.hefquin.engine.query;

import se.liu.ida.hefquin.engine.query.impl.MatchCypherQuery;
import se.liu.ida.hefquin.engine.query.impl.UnionCypherQuery;

import java.util.Set;

public interface CypherQuery {
    String toString();

    Set<String> getMatches();
    Set<String> getConditions();
    Set<String> getReturnExprs();
    Set<CypherQuery> getUnion();
    Set<CypherQuery> getIntersect();

    void addMatchClause(final String match);
    void addConditionConjunction(final String cond);
    void addReturnClause(final String ret);
    void addQueryToUnion(final CypherQuery q);

    boolean isMatchQuery();
    boolean isUnionQuery();

    CypherQuery combineWith(final CypherQuery query);
    CypherQuery combineWithMatch(final MatchCypherQuery query);
    CypherQuery combineWithUnion(final UnionCypherQuery query);

    boolean isCompatibleWith(final CypherQuery query);
}
