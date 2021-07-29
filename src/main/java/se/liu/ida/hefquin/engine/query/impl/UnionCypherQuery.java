package se.liu.ida.hefquin.engine.query.impl;

import se.liu.ida.hefquin.engine.query.CypherQuery;
import se.liu.ida.hefquin.engine.query.cypher.CypherVar;
import se.liu.ida.hefquin.engine.query.cypher.MatchClause;
import se.liu.ida.hefquin.engine.query.cypher.ReturnStatement;
import se.liu.ida.hefquin.engine.query.cypher.WhereCondition;

import java.util.*;
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

    public UnionCypherQuery(Set<CypherQuery> result) {
        this.union = result;
    }

    @Override
    public Set<MatchClause> getMatches() {
        throw new UnsupportedOperationException("Not a MATCH query");
    }

    @Override
    public Set<WhereCondition> getConditions() {
        throw new UnsupportedOperationException("Not a MATCH query");
    }

    @Override
    public Set<ReturnStatement> getReturnExprs() {
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
    public void addMatchClause(final MatchClause match) {
        throw new UnsupportedOperationException("Not a MATCH query");
    }

    @Override
    public void addConditionConjunction(final WhereCondition cond) {
        throw new UnsupportedOperationException("Not a MATCH query");
    }

    @Override
    public void addReturnClause(final ReturnStatement ret) {
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
        final Set<CypherQuery> result = new HashSet<>();
        for (final CypherQuery q : this.union) {
            result.add(q.combineWith(query));
        }
        return new UnionCypherQuery(result);
    }

    @Override
    public CypherQuery combineWithUnion(final UnionCypherQuery query) {
        final Set<CypherQuery> result = new HashSet<>();
        for (final CypherQuery q1 : this.union) {
            for (final CypherQuery q2 : query.union) {
                result.add(q1.combineWith(q2));
            }
        }
        removeMalformed(result);
        return new UnionCypherQuery(result);
    }

    private void removeMalformed(final Set<CypherQuery> queries) {
        final List<CypherQuery> toRemove = new LinkedList<>();
        for (final CypherQuery q : queries) {
            final List<String> seenAliases = new ArrayList<>();
            for (final ReturnStatement r : q.getReturnExprs()) {
                if (seenAliases.contains(r.getAlias())){
                    toRemove.add(q);
                    break;
                } else {
                    seenAliases.add(r.getAlias());
                }
            }
        }
        toRemove.forEach(queries::remove);
    }

    @Override
    public Set<CypherVar> getMatchVars() {
        return union.stream().map(CypherQuery::getMatchVars).flatMap(Set::stream).collect(Collectors.toSet());
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
