package se.liu.ida.hefquin.engine.query.utils;

import se.liu.ida.hefquin.engine.query.CypherQuery;
import se.liu.ida.hefquin.engine.query.cypher.MatchClause;
import se.liu.ida.hefquin.engine.query.cypher.ReturnStatement;
import se.liu.ida.hefquin.engine.query.cypher.WhereCondition;
import se.liu.ida.hefquin.engine.query.impl.MatchCypherQuery;

public class CypherQueryBuilder {

    private final CypherQuery query;

    private CypherQueryBuilder() {
        this.query = new MatchCypherQuery();
    }

    public static CypherQueryBuilder newBuilder() {
        return new CypherQueryBuilder();
    }

    public CypherQueryBuilder match(final MatchClause match) {
        this.query.addMatchClause(match);
        return this;
    }

    public CypherQueryBuilder condition(final WhereCondition condition) {
        this.query.addConditionConjunction(condition);
        return this;
    }

    public CypherQueryBuilder returns(final ReturnStatement ret) {
        this.query.addReturnClause(ret);
        return this;
    }

    public CypherQuery build() {
        return this.query;
    }

}
