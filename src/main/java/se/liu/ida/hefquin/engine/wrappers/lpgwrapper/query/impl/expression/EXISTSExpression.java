package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherExpression;

import java.util.Objects;
import java.util.Set;

public class EXISTSExpression implements BooleanCypherExpression{

    protected final CypherExpression expression;

    public EXISTSExpression(final CypherExpression expression) {
        assert expression != null;
        this.expression = expression;
    }

    @Override
    public Set<CypherVar> getVars() {
        return expression.getVars();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EXISTSExpression that = (EXISTSExpression) o;
        return expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }

    @Override
    public String toString() {
        return "EXISTS(" + expression + ')';
    }
}
