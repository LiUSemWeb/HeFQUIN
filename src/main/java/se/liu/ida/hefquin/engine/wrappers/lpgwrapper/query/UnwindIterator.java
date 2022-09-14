package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;

import java.util.List;

/**
 * This interface represents an UNWIND clause of a Match Query.
 * The relevant UNWIND clauses look like this:
 *  UNWIND [tempvar IN listExpression WHERE filters | [returnExpressions]] AS alias
 */
public interface UnwindIterator {

    /**
     * Returns the inner, temporary variable that iterates through the values of listExpression
     */
    CypherVar getInnerVar();

    /**
     * Returns the expression that evaluates to a list, whose values are iterated through.
     * TODO: this should return a CypherExpression Object
     */
    String getListExpression();

    /**
     * Returns the list of conditions that must evaluate to TRUE, for an element in listExpression
     * to be considered in the final result.
     */
    List<WhereCondition> getFilters();

    /**
     * Returns the list of expressions that are returned for each element in listExpression that
     * passes the filters. E.g.: k, a[k], etc.
     */
    List<String> getReturnExpressions();

    /**
     * Returns the CypherVar object that each set of return expressions is aliased as.
     */
    CypherVar getAlias();
}
