package se.liu.ida.hefquin.engine.query.impl;

import se.liu.ida.hefquin.engine.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.query.cypher.CypherVar;
import se.liu.ida.hefquin.engine.query.cypher.MatchClause;
import se.liu.ida.hefquin.engine.query.cypher.ReturnStatement;
import se.liu.ida.hefquin.engine.query.cypher.WhereCondition;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CypherMatchQueryImpl implements CypherMatchQuery {

    final protected Set<MatchClause> matches;
    final protected Set<WhereCondition> conditions;
    final protected Set<ReturnStatement> returnExprs;

    public CypherMatchQueryImpl() {
        matches = new HashSet<>();
        conditions = new HashSet<>();
        returnExprs = new HashSet<>();
    }

    public CypherMatchQueryImpl(final Set<MatchClause> matches, final Set<WhereCondition> conditions,
                                final Set<ReturnStatement> returnExprs) {
        this.matches = matches;
        this.conditions = conditions;
        this.returnExprs = returnExprs;
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

    private boolean compatibleVars(Set<CypherVar> inVars, Set<CypherVar> currentVars) {
        return currentVars.containsAll(inVars);
    }

    @Override
    public Set<CypherVar> getMatchVars() {
        final Set<CypherVar> result = new HashSet<>();
        for (final MatchClause m : matches){
            result.addAll(m.getVars());
        }
        return result;
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
        if (!(o instanceof CypherMatchQueryImpl)) return false;
        CypherMatchQueryImpl that = (CypherMatchQueryImpl) o;
        return matches.equals(that.matches) && conditions.equals(that.conditions) && returnExprs.equals(that.returnExprs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matches, conditions, returnExprs);
    }
}
