package se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression;

import java.util.Objects;
import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherExpressionVisitor;

public class PropertyAccessExpression implements CypherExpression {

    protected final CypherVar var;
    protected final String property;

    public PropertyAccessExpression(final CypherVar var, final String property) {
        assert var != null;
        assert property != null;

        this.var = var;
        this.property = property;
    }

    @Override
    public Set<CypherVar> getVars() {
        return Set.of(var);
    }

    @Override
    public void visit(final CypherExpressionVisitor visitor) {
        var.visit(visitor);
        visitor.visitPropertyAccess(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyAccessExpression that = (PropertyAccessExpression) o;
        return var.equals(that.var) && property.equals(that.property);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, property);
    }

    @Override
    public String toString() {
        return var + "." + property;
    }

    public String getProperty() {
        return property;
    }
}
