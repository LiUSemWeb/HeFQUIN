package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.CypherMatchQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.AliasedExpression;

import java.util.ArrayList;
import java.util.List;

public class CypherQueryBuilder {

    private final List<MatchClause> matches;
    private final List<WhereCondition> conditions;
    private final List<UnwindIterator> iterators;
    private final List<AliasedExpression> returns;

    public CypherQueryBuilder() {
        this.matches = new ArrayList<>();
        this.conditions = new ArrayList<>();
        this.iterators = new ArrayList<>();
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

    public CypherQueryBuilder addIterator(final UnwindIterator iterator) {
        this.iterators.add(iterator);
        return this;
    }

    public CypherQueryBuilder addReturn(final AliasedExpression ret) {
        this.returns.add(ret);
        return this;
    }

    public CypherQueryBuilder add(final Object clause) {
        if (clause instanceof MatchClause) {
            this.addMatch((MatchClause) clause);
        } else if (clause instanceof WhereCondition) {
            this.addCondition((WhereCondition) clause);
        } else if (clause instanceof UnwindIterator) {
            this.addIterator((UnwindIterator) clause);
        } else if (clause instanceof AliasedExpression) {
            this.addReturn((AliasedExpression) clause);
        } else {
            throw new IllegalArgumentException("Provided object is not a CypherQuery Clause");
        }
        return this;
    }

    public CypherMatchQuery build() {
        return new CypherMatchQueryImpl(matches, conditions, iterators, returns);
    }

    public CypherQueryBuilder addAll(final CypherMatchQuery q) {
        for (final MatchClause m : q.getMatches()){
            this.addMatch(m);
        }
        for (final WhereCondition c : q.getConditions()) {
            this.addCondition(c);
        }
        for (final UnwindIterator i : q.getIterators()) {
            this.addIterator(i);
        }
        for (final AliasedExpression r : q.getReturnExprs()) {
            this.addReturn(r);
        }
        return this;
    }
}
