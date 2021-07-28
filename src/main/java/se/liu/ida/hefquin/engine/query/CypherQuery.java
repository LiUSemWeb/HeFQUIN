package se.liu.ida.hefquin.engine.query;

import se.liu.ida.hefquin.engine.query.cypher.MatchClause;
import se.liu.ida.hefquin.engine.query.cypher.ReturnStatement;
import se.liu.ida.hefquin.engine.query.cypher.WhereCondition;
import se.liu.ida.hefquin.engine.query.impl.MatchCypherQuery;
import se.liu.ida.hefquin.engine.query.impl.UnionCypherQuery;

import java.util.Set;

public interface CypherQuery {
    String toString();

    Set<MatchClause> getMatches();
    Set<WhereCondition> getConditions();
    Set<ReturnStatement> getReturnExprs();
    Set<CypherQuery> getUnion();
    Set<CypherQuery> getIntersect();

    void addMatchClause(final MatchClause match);
    void addConditionConjunction(final WhereCondition cond);
    void addReturnClause(final ReturnStatement ret);
    void addQueryToUnion(final CypherQuery q);

    boolean isMatchQuery();
    boolean isUnionQuery();

    CypherQuery combineWith(final CypherQuery query);
    CypherQuery combineWithMatch(final MatchCypherQuery query);
    CypherQuery combineWithUnion(final UnionCypherQuery query);
}
