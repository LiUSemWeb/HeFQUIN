package se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression;

import java.util.Objects;
import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherExpressionVisitor;

public class VariableLabelExpression implements BooleanCypherExpression {

    protected final CypherVar variable;
    protected final String label;

    public VariableLabelExpression(final CypherVar variable, final String label) {
        assert variable != null;
        assert label != null;

        this.variable = variable;
        this.label = label;
    }

    @Override
    public Set<CypherVar> getVars() {
        return Set.of(variable);
    }

    @Override
    public void visit(final CypherExpressionVisitor visitor) {
        variable.visit(visitor);
        visitor.visitVariableLabel(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariableLabelExpression that = (VariableLabelExpression) o;
        return variable.equals(that.variable) && label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable, label);
    }

    @Override
    public String toString() {
        return variable + ":" + label;
    }

    public String getLabel() {
        return label;
    }
}
