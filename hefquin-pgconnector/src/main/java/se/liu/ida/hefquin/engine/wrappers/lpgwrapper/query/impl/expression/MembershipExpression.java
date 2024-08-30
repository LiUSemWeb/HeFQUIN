package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherExpressionVisitor;

public class MembershipExpression implements BooleanCypherExpression {

    protected final CypherVar var;
    protected final ListCypherExpression listExpression;

    public MembershipExpression(final CypherVar var, final ListCypherExpression listExpression) {
        assert var != null;
        assert listExpression != null;

        this.var = var;
        this.listExpression = listExpression;
    }

    @Override
    public Set<CypherVar> getVars() {
        final Set<CypherVar> res = new HashSet<>(listExpression.getVars());
        res.add(var);
        return res;
    }

    @Override
    public void visit(final CypherExpressionVisitor visitor) {
        var.visit(visitor);
        listExpression.visit(visitor);
        visitor.visitMembership(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MembershipExpression that = (MembershipExpression) o;
        return var.equals(that.var) && listExpression.equals(that.listExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, listExpression);
    }

    @Override
    public String toString() {
        return var + " IN " + listExpression;
    }
}
