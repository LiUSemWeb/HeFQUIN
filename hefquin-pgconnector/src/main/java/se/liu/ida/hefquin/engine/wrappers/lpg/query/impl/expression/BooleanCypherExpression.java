package se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression;

import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherExpression;

/**
 * This interface represents the subset of Cypher Expressions that can be evaluated
 * to a boolean value: true, false or null.
 * e.g., v1.name="Uma Thurman" is an expression that has a boolean result
 */
public interface BooleanCypherExpression extends CypherExpression {
}
