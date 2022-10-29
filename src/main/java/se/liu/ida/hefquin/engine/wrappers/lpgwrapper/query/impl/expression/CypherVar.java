package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherExpression;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherExpressionVisitor;

import java.util.Objects;
import java.util.Set;

public class CypherVar implements CypherExpression {
    private final String name;

    public CypherVar(final String name) {
        assert name != null;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CypherVar)) return false;
        CypherVar cypherVar = (CypherVar) o;
        return name.equals(cypherVar.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Set<CypherVar> getVars() {
        return Set.of(this);
    }

    @Override
    public void acceptVisitor(final CypherExpressionVisitor visitor) {
        visitor.visitVar(this);
    }
}
