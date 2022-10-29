package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherExpressionVisitor;

import java.util.Collections;
import java.util.Set;

public class CountLargerThanZeroExpression implements BooleanCypherExpression{
    @Override
    public Set<CypherVar> getVars() {
        return Collections.emptySet();
    }

    @Override
    public void acceptVisitor(final CypherExpressionVisitor visitor) {
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
