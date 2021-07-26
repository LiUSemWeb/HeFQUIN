package se.liu.ida.hefquin.engine.query.impl;

import se.liu.ida.hefquin.engine.query.CypherQuery;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class UnionCypherQuery implements CypherQuery {

    final Set<CypherQuery> union;

    public UnionCypherQuery() {
        union = new HashSet<>();
    }

    public UnionCypherQuery(CypherQuery... queries) {
        this();
        union.addAll(Arrays.asList(queries));
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
    public Set<CypherQuery> getIntersect() {
        throw new UnsupportedOperationException("Not an INTERSECT query");
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

    @Override
    public CypherQuery combineWith(final CypherQuery query) {
        return query.combineWithUnion(this);
    }

    @Override
    public CypherQuery combineWithMatch(final MatchCypherQuery query) {
        throw new UnsupportedOperationException("Unable to combine a MATCH and a UNION query");
    }

    @Override
    public CypherQuery combineWithUnion(final UnionCypherQuery query) {
        return null;
    }

    @Override
    public String toString() {
        return union.stream().map(CypherQuery::toString).collect(Collectors.joining("\nUNION\n"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnionCypherQuery)) return false;
        UnionCypherQuery that = (UnionCypherQuery) o;
        return union.equals(that.union);
    }

    @Override
    public int hashCode() {
        return Objects.hash(union);
    }
}
