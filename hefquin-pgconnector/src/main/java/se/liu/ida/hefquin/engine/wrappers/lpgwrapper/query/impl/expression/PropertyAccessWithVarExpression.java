package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression;

import java.util.Objects;
import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherExpression;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherExpressionVisitor;

public class PropertyAccessWithVarExpression implements CypherExpression {

    protected final CypherVar var;
    protected final CypherVar innerVar;

    public PropertyAccessWithVarExpression(final CypherVar var, final CypherVar innerVar) {
        assert var != null;
        assert innerVar != null;

        this.var = var;
        this.innerVar = innerVar;
    }

    @Override
    public Set<CypherVar> getVars() {
        return Set.of(var, innerVar);
    }

    @Override
    public void visit(final CypherExpressionVisitor visitor) {
        var.visit(visitor);
        innerVar.visit(visitor);
        visitor.visitPropertyAccessWithVar(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyAccessWithVarExpression that = (PropertyAccessWithVarExpression) o;
        return var.equals(that.var) && innerVar.equals(that.innerVar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, innerVar);
    }

    @Override
    public String toString() {
        return var + "[" + innerVar + "]";
    }
}
