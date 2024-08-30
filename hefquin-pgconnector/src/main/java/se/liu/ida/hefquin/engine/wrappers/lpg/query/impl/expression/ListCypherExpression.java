package se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression;

import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherExpression;

/**
 * This interface represents elements in the subset of Cypher Expressions that can be evaluated to lists.
 * e.g.: KEYS(v1) is an expression that produces a list
 */
public interface ListCypherExpression extends CypherExpression {
}
