package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherExpressionVisitor;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.VariableReplacementVisitor;

import java.util.Set;

/**
 * This interface represents a generic Cypher Expression. As per the grammar defining expressions
 * in Figure 4 here: https://arxiv.org/pdf/1802.09984.pdf
 * Thus, a variable, a function call, accessing a property value, etc. are Cypher expressions
 */
public interface CypherExpression {

    /**
     * Returns a list of the [global] Cypher Variables used within the expression
     */
    Set<CypherVar> getVars();

    /**
     * accepts a {@link CypherExpressionVisitor} and propagates the visit recursively when required.
     */
    void acceptVisitor(final CypherExpressionVisitor visitor);
}
