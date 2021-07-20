package se.liu.ida.hefquin.engine.query;

import se.liu.ida.hefquin.engine.query.impl.MatchCypherQuery;

public class CypherQueryBuilder {

    private final CypherQuery query;

    private CypherQueryBuilder() {
        this.query = new MatchCypherQuery();
    }

    public static CypherQueryBuilder newBuilder() {
        return new CypherQueryBuilder();
    }

    public CypherQueryBuilder match(final String match) {
        this.query.addMatchClause(match);
        return this;
    }

    public CypherQueryBuilder condition(final String condition) {
        this.query.addConditionConjunction(condition);
        return this;
    }

    public CypherQueryBuilder returns(final String ret) {
        this.query.addReturnClause(ret);
        return this;
    }

    public CypherQuery build() {
        return this.query;
    }

}
