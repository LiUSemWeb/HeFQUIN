package se.liu.ida.hefquin.engine.query.impl;

import se.liu.ida.hefquin.engine.query.CypherQuery;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MatchCypherQuery implements CypherQuery {

    final Set<String> matches;
    final Set<String> conditions;
    final Set<String> returnExprs;

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
    public void addMatchClause(final String match) {
        matches.add(match);
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (matches.size()>0) {
            builder.append(String.join(" ", matches));
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
}
