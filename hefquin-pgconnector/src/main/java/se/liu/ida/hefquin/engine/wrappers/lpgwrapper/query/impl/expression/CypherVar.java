package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression;

import java.util.Objects;
import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherExpression;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherExpressionVisitor;

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
    public void visit(final CypherExpressionVisitor visitor) {
        visitor.visitVar(this);
    }
}
