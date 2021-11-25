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

    public CypherQueryBuilder() {
        this.matches = new ArrayList<>();
        this.conditions = new ArrayList<>();
        this.returns = new ArrayList<>();
    }

    public CypherQueryBuilder addMatch(final MatchClause match) {
        this.matches.add(match);
        return this;
    }

    public CypherQueryBuilder addCondition(final WhereCondition condition) {
        this.conditions.add(condition);
        return this;
    }

    public CypherQueryBuilder addReturn(final ReturnStatement ret) {
        this.returns.add(ret);
        return this;
    }

    public CypherQueryBuilder add(final Object clause) {
        if (clause instanceof MatchClause) {
            this.addMatch((MatchClause) clause);
        } else if (clause instanceof WhereCondition) {
            this.addCondition((WhereCondition) clause);
        } else if (clause instanceof ReturnStatement) {
            this.addReturn((ReturnStatement) clause);
        } else {
            throw new IllegalArgumentException("Provided object is not a CypherQuery Clause");
        }
        return this;
    }

    public CypherMatchQuery build() {
        return new CypherMatchQueryImpl(matches, conditions, returns);
    }
}
