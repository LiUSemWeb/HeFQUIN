package se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression;

import java.util.*;
import java.util.stream.Collectors;

import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.UnwindIterator;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherExpressionVisitor;

public class UnwindIteratorImpl implements UnwindIterator {

    protected final CypherVar innerVar;
    protected final ListCypherExpression listExpression;
    protected final List<BooleanCypherExpression> filters;
    protected final List<CypherExpression> returnExpressions;
    protected final CypherVar alias;

    public UnwindIteratorImpl(final CypherVar innerVar, final ListCypherExpression listExpression,
                              final List<BooleanCypherExpression> filters,
                              final List<CypherExpression> returnExpressions, final CypherVar alias) {
        assert innerVar != null;
        assert listExpression != null;
        assert alias != null;

        this.innerVar = innerVar;
        this.listExpression = listExpression;
        this.filters = Objects.requireNonNullElseGet(filters, ArrayList::new);
        this.returnExpressions = returnExpressions;
        this.alias = alias;
    }

    @Override
    public CypherVar getInnerVar() {
        return innerVar;
    }

    @Override
    public ListCypherExpression getListExpression() {
        return listExpression;
    }

    @Override
    public List<BooleanCypherExpression> getFilters() {
        return filters;
    }

    @Override
    public List<CypherExpression> getReturnExpressions() {
        return returnExpressions;
    }

    @Override
    public CypherVar getAlias() {
        return alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnwindIteratorImpl that = (UnwindIteratorImpl) o;
        return innerVar.equals(that.innerVar) && listExpression.equals(that.listExpression)
                && Objects.equals(filters, that.filters)
                && Objects.equals(returnExpressions, that.returnExpressions)
                && alias.equals(that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(innerVar, listExpression, filters, returnExpressions, alias);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("UNWIND [")
                .append(innerVar)
                .append(" IN ")
                .append(listExpression);
        if (filters != null && !filters.isEmpty()) {
            builder.append(" WHERE ");
            builder.append(filters.stream().map(Objects::toString).collect(Collectors.joining(" AND ")));
        }
        builder.append(" | [")
                .append(returnExpressions.stream().map(Objects::toString).collect(Collectors.joining(", ")))
                .append("]] AS ")
                .append(alias);
        return builder.toString();
    }

    @Override
    public Set<CypherVar> getVars() {
        final Set<CypherVar> res = new HashSet<>(listExpression.getVars());
        for (final CypherExpression c : filters)
            res.addAll(c.getVars());
        for (final CypherExpression c : returnExpressions)
            res.addAll(c.getVars());
        res.add(alias);
        return res;
    }

    @Override
    public void visit(final CypherExpressionVisitor visitor) {
        innerVar.visit(visitor);
        listExpression.visit(visitor);
        for (final CypherExpression e : filters)
            e.visit(visitor);
        for (final CypherExpression e : returnExpressions)
            e.visit(visitor);
        alias.visit(visitor);
        visitor.visitUnwind(this);
    }
}
