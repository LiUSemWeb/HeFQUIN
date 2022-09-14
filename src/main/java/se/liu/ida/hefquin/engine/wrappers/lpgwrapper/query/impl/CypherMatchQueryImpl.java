package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;

import java.util.*;
import java.util.stream.Collectors;

public class CypherMatchQueryImpl implements CypherMatchQuery {

    final protected List<MatchClause> matches;
    final protected List<WhereCondition> conditions;

    final protected List<UnwindIterator> iterators;
    final protected List<ReturnStatement> returnExprs;

    public CypherMatchQueryImpl() {
        matches = new ArrayList<>();
        conditions = new ArrayList<>();
        returnExprs = new ArrayList<>();
        iterators = new ArrayList<>();
    }

    public CypherMatchQueryImpl(final List<MatchClause> matches, final List<WhereCondition> conditions,
                                final List<UnwindIterator> iterators, final List<ReturnStatement> returnExprs) {
        assert matches != null;
        assert  returnExprs != null;

        this.matches = matches;
        this.conditions = conditions;
        this.returnExprs = returnExprs;
        this.iterators = iterators;
    }

    @Override
    public List<MatchClause> getMatches() {
        return matches;
    }

    @Override
    public List<WhereCondition> getConditions() {
        return conditions;
    }

    @Override
    public List<UnwindIterator> getIterators() {
        return iterators;
    }

    @Override
    public List<ReturnStatement> getReturnExprs() {
        return returnExprs;
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
        if (conditions!=null && conditions.size()>0) {
            builder.append("WHERE ");
            builder.append(conditions.stream().map(Objects::toString).collect(Collectors.joining(" AND ")));
            builder.append("\n");
        }
        if (iterators!=null && iterators.size()>0) {
            for (final UnwindIterator it : iterators) {
                builder.append(it);
                builder.append("\n");
            }
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
        if (o == null || getClass() != o.getClass()) return false;
        CypherMatchQueryImpl that = (CypherMatchQueryImpl) o;
        return matches.equals(that.matches) && Objects.equals(conditions, that.conditions) && Objects.equals(iterators, that.iterators) && returnExprs.equals(that.returnExprs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matches, conditions, iterators, returnExprs);
    }
}
