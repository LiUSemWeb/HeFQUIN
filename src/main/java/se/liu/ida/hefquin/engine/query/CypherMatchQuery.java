package se.liu.ida.hefquin.engine.query;

import se.liu.ida.hefquin.engine.query.cypher.MatchClause;
import se.liu.ida.hefquin.engine.query.cypher.ReturnStatement;
import se.liu.ida.hefquin.engine.query.cypher.WhereCondition;

import java.util.List;

public interface CypherMatchQuery extends CypherQuery {

    List<MatchClause> getMatches();
    List<WhereCondition> getConditions();
    List<ReturnStatement> getReturnExprs();

}
