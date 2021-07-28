package se.liu.ida.hefquin.engine.query.impl;

import se.liu.ida.hefquin.engine.query.CypherQuery;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class MatchCypherQuery implements CypherQuery {

    final protected Set<String> matches;
    final protected Set<String> conditions;
    final protected Set<String> returnExprs;

    public MatchCypherQuery() {
        matches = new HashSet<>();
        conditions = new HashSet<>();
        returnExprs = new HashSet<>();
    }

    public MatchCypherQuery(final String[] matches, final String[] conditions, final String[] returns) {
        this();
        this.matches.addAll(Arrays.asList(matches));
        this.conditions.addAll(Arrays.asList(conditions));
        this.returnExprs.addAll(Arrays.asList(returns));
    }

    @Override
    public Set<String> getMatches() {
        return matches;
    }

    @Override
    public Set<String> getConditions() {
        return conditions;
    }

    @Override
    public Set<String> getReturnExprs() {
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
    public void addMatchClause(final String match) {
        if (!addsRedundantClause(match))
            matches.add(match);
    }

    private boolean addsRedundantClause(final String match) {
        for (final String m : matches){
            if (m.contains(match) || match.contains(m))
                return true;
        }
        return false;
    }

    @Override
    public void addConditionConjunction(final String cond) {
        conditions.add(cond);
    }

    @Override
    public void addReturnClause(final String ret) {
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
        for (final String match : query.getMatches()) {
            result.addMatchClause(match);
        }
        for (final String cond : query.getConditions()) {
            result.addConditionConjunction(cond);
        }
        for (final String ret : query.getReturnExprs()){
            result.addReturnClause(ret);
        }
        for (final String match : this.getMatches()) {
            result.addMatchClause(match);
        }
        for (final String cond : this.getConditions()) {
            result.addConditionConjunction(cond);
        }
        for (final String ret : this.getReturnExprs()){
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
            builder.append("MATCH ");
            builder.append(String.join(" MATCH ", matches));
            builder.append("\n");
        }
        if (conditions.size()>0) {
            builder.append("WHERE ");
            builder.append(String.join(" AND ", conditions));
            builder.append("\n");
        }
        if (returnExprs.size()>0) {
            builder.append("RETURN ");
            builder.append(String.join(", ", returnExprs));
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
