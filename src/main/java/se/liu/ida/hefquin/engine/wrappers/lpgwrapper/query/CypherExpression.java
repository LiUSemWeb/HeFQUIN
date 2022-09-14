package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;

import java.util.Set;

public interface CypherExpression {

    Set<CypherVar> getVars();

}
