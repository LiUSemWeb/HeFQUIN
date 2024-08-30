package se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression;

import java.util.Objects;
import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherExpressionVisitor;

public class KeysExpression implements ListCypherExpression{

    protected final CypherVar var;

    public KeysExpression(final CypherVar var) {
        assert var != null;
        this.var = var;
    }

    @Override
    public Set<CypherVar> getVars() {
        return Set.of(var);
    }

    @Override
    public void visit(final CypherExpressionVisitor visitor) {
        var.visit(visitor);
        visitor.visitKeys(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeysExpression that = (KeysExpression) o;
        return var.equals(that.var);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var);
    }

    @Override
    public String toString() {
        return "KEYS(" +var + ')';
    }
}
