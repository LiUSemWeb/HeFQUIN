package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherExpression;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherExpressionVisitor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class EqualityExpression implements BooleanCypherExpression{

    protected final CypherExpression leftExpression;
    protected final CypherExpression rightExpression;

    public EqualityExpression(final CypherExpression leftExpression, final CypherExpression rightExpression) {
        assert leftExpression != null;
        assert rightExpression != null;

        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }

    @Override
    public Set<CypherVar> getVars() {
        final Set<CypherVar> res = new HashSet<>(leftExpression.getVars());
        res.addAll(rightExpression.getVars());
        return res;
    }

    @Override
    public void visit(final CypherExpressionVisitor visitor) {
        leftExpression.visit(visitor);
        rightExpression.visit(visitor);
        visitor.visitEquality(this);
    }

    public CypherExpression getLeftExpression() {
        return leftExpression;
    }

    public CypherExpression getRightExpression() {
        return rightExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EqualityExpression that = (EqualityExpression) o;
        return leftExpression.equals(that.leftExpression) && rightExpression.equals(that.rightExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftExpression, rightExpression);
    }

    @Override
    public String toString() {
        return leftExpression + "=" + rightExpression;
    }
}
