package se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression;

import java.util.Collections;
import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherExpressionVisitor;

public class CountLargerThanZeroExpression implements BooleanCypherExpression{
    @Override
    public Set<CypherVar> getVars() {
        return Collections.emptySet();
    }

    @Override
    public void visit(final CypherExpressionVisitor visitor) {
        visitor.visitCountLargerThanZero(this);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CountLargerThanZeroExpression;
    }

    @Override
    public String toString() {
        return "COUNT(*) > 0";
    }
}
