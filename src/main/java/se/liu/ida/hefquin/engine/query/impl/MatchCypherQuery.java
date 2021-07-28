package se.liu.ida.hefquin.engine.query.impl;

import se.liu.ida.hefquin.engine.query.CypherQuery;
import se.liu.ida.hefquin.engine.query.cypher.MatchClause;
import se.liu.ida.hefquin.engine.query.cypher.ReturnStatement;
import se.liu.ida.hefquin.engine.query.cypher.WhereCondition;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class MatchCypherQuery implements CypherQuery {

    final protected Set<MatchClause> matches;
    final protected Set<WhereCondition> conditions;
    final protected Set<ReturnStatement> returnExprs;

    public MatchCypherQuery() {
        matches = new HashSet<>();
        conditions = new HashSet<>();
        returnExprs = new HashSet<>();
    }

    @Override
    public Set<MatchClause> getMatches() {
        return matches;
    }

    @Override
    public Set<WhereCondition> getConditions() {
        return conditions;
    }

    @Override
    public Set<ReturnStatement> getReturnExprs() {
        return returnExprs;
    }

    @Override
    public Set<CypherQuery> getUnion() {
        throw new UnsupportedOperationException("Not a UNION query");
    }

    @Override
    public Set<CypherQuery> getIntersect() {
        throw new UnsupportedOperationException("Not an INTERSECT query");
    }

    @Override
    public void addMatchClause(final MatchClause match) {
        if (!addsRedundantClause(match))
            matches.add(match);
    }

    private boolean addsRedundantClause(final MatchClause match) {
        for (final MatchClause m : matches){
            if (m.isRedundantWith(match))
                return true;
        }
        return false;
    }

    @Override
    public void addConditionConjunction(final WhereCondition cond) {
        conditions.add(cond);
    }

    @Override
    public void addReturnClause(final ReturnStatement ret) {
        returnExprs.add(ret);
    }

    @Override
    public void addQueryToUnion(final CypherQuery q) {
        throw new UnsupportedOperationException("Not a MATCH query");
    }

    @Override
    public boolean isMatchQuery() {
        return true;
    }

    @Override
    public boolean isUnionQuery() {
        return false;
    }

    @Override
    public CypherQuery combineWith(final CypherQuery query) {
        return query.combineWithMatch(this);
    }

    @Override
    public CypherQuery combineWithMatch(final MatchCypherQuery query) {
        CypherQuery result = new MatchCypherQuery();
        for (final MatchClause match : query.getMatches()) {
            result.addMatchClause(match);
        }
        for (final WhereCondition cond : query.getConditions()) {
            result.addConditionConjunction(cond);
        }
        for (final ReturnStatement ret : query.getReturnExprs()){
            result.addReturnClause(ret);
        }
        for (final MatchClause match : this.getMatches()) {
            result.addMatchClause(match);
        }
        for (final WhereCondition cond : this.getConditions()) {
            result.addConditionConjunction(cond);
        }
        for (final ReturnStatement ret : this.getReturnExprs()){
            result.addReturnClause(ret);
        }
        return result;
    }

    @Override
    public CypherQuery combineWithUnion(UnionCypherQuery query) {
        final Set<CypherQuery> result = new HashSet<>();
        for (final CypherQuery q : query.union) {
            result.add(q.combineWithMatch(this));
        }
        return new UnionCypherQuery(result);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (matches.size()>0) {
            builder.append(matches.stream().map(Objects::toString).collect(Collectors.joining("\n")));
            builder.append("\n");
        }
        if (conditions.size()>0) {
            builder.append("WHERE ");
            builder.append(conditions.stream().map(Objects::toString).collect(Collectors.joining(" AND ")));
            builder.append("\n");
        }
        if (returnExprs.size()>0) {
            builder.append("RETURN ");
            builder.append(returnExprs.stream().map(Objects::toString).collect(Collectors.joining(", ")));
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchCypherQuery)) return false;
        MatchCypherQuery that = (MatchCypherQuery) o;
        return matches.equals(that.matches) && conditions.equals(that.conditions) && returnExprs.equals(that.returnExprs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matches, conditions, returnExprs);
    }
}
