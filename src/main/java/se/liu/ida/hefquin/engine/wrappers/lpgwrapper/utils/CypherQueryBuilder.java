package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.MatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.ReturnStatement;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.WhereCondition;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.CypherMatchQueryImpl;

import java.util.ArrayList;
import java.util.List;

public class CypherQueryBuilder {

    private final List<MatchClause> matches;
    private final List<WhereCondition> conditions;
    private final List<ReturnStatement> returns;

    private CypherQueryBuilder() {
        this.matches = new ArrayList<>();
        this.conditions = new ArrayList<>();
        this.returns = new ArrayList<>();
    }

    public static CypherQueryBuilder newBuilder() {
        return new CypherQueryBuilder();
    }

    public CypherQueryBuilder match(final MatchClause match) {
        this.matches.add(match);
        return this;
    }

    public CypherQueryBuilder condition(final WhereCondition condition) {
        this.conditions.add(condition);
        return this;
    }

    public CypherQueryBuilder returns(final ReturnStatement ret) {
        this.returns.add(ret);
        return this;
    }

    public CypherMatchQuery build() {
        return new CypherMatchQueryImpl(matches, conditions, returns);
    }
}
