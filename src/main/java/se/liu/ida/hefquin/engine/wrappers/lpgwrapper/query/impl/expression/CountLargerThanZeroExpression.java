package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression;

import java.util.Collections;
import java.util.Set;

public class CountLargerThanZeroExpression implements BooleanCypherExpression{
    @Override
    public Set<CypherVar> getVars() {
        return Collections.emptySet();
    }
}
