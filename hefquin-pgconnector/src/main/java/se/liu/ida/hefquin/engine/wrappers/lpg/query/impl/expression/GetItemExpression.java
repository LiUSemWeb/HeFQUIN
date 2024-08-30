package se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression;

import java.util.Objects;
import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherExpressionVisitor;

public class GetItemExpression implements CypherExpression {

    protected final CypherExpression expression;
    protected final int index;

    public GetItemExpression(final CypherExpression expression, final int index) {
        assert expression != null;

        this.expression = expression;
        this.index = index;
    }

    @Override
    public Set<CypherVar> getVars() {
        return expression.getVars();
    }

    @Override
    public void visit(final CypherExpressionVisitor visitor) {
        expression.visit(visitor);
        visitor.visitGetItem(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetItemExpression that = (GetItemExpression) o;
        return index == that.index && expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, index);
    }

    @Override
    public String toString() {
        return expression + "[" + index + "]";
    }

    public int getIndex() {
        return index;
    }

    public CypherExpression getExpression() {
        return expression;
    }
}
