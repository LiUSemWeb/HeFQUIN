package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherExpression;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class LiteralExpression implements CypherExpression {

    protected final String value;

    public LiteralExpression(final String value) {
        assert value != null;
        this.value = value;
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.emptySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LiteralExpression that = (LiteralExpression) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }
}
