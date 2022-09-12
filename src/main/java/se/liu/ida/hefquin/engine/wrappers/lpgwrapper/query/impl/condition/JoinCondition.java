package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.condition;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.WhereCondition;

import java.util.Objects;
import java.util.Set;

public class JoinCondition implements WhereCondition {

    protected final String expr1;
    protected final String expr2;

    public JoinCondition(final String expr1, final String expr2) {
        assert expr1 != null;
        assert expr2 != null;

        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    @Override
    public Set<CypherVar> getVars() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoinCondition that = (JoinCondition) o;
        return expr1.equals(that.expr1) && expr2.equals(that.expr2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expr1, expr2);
    }

    @Override
    public String toString() {
        return expr1+"="+expr2;
    }
}
