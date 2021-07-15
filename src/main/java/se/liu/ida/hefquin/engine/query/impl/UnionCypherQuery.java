package se.liu.ida.hefquin.engine.query.impl;

import se.liu.ida.hefquin.engine.query.CypherQuery;

import java.util.HashSet;
import java.util.Set;

public class UnionCypherQuery implements CypherQuery {

    final Set<CypherQuery> union;

    public UnionCypherQuery() {
        union = new HashSet<>();
    }

    @Override
    public Set<String> getMatches() {
        throw new UnsupportedOperationException("Not a MATCH query");
    }

    @Override
    public Set<String> getConditions() {
        throw new UnsupportedOperationException("Not a MATCH query");
    }

    @Override
    public Set<String> getReturnExprs() {
        throw new UnsupportedOperationException("Not a MATCH query");
    }

    @Override
    public Set<CypherQuery> getUnion() {
        return union;
    }

    @Override
    public void addMatchClause(final String match) {
        throw new UnsupportedOperationException("Not a MATCH query");
    }

    @Override
    public void addConditionConjunction(final String cond) {
        throw new UnsupportedOperationException("Not a MATCH query");
    }

    @Override
    public void addReturnClause(final String ret) {
        throw new UnsupportedOperationException("Not a MATCH query");
    }

    @Override
    public void addQueryToUnion(final CypherQuery q) {
        union.add(q);
    }

    @Override
    public boolean isMatchQuery() {
        return false;
    }

    @Override
    public boolean isUnionQuery() {
        return true;
    }
}
