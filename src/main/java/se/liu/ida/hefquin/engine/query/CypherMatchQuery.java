package se.liu.ida.hefquin.engine.query;

import se.liu.ida.hefquin.engine.query.cypher.MatchClause;
import se.liu.ida.hefquin.engine.query.cypher.ReturnStatement;
import se.liu.ida.hefquin.engine.query.cypher.WhereCondition;

import java.util.Set;

public interface CypherMatchQuery extends CypherQuery {

    Set<MatchClause> getMatches();
    Set<WhereCondition> getConditions();
    Set<ReturnStatement> getReturnExprs();

}
