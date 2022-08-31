package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.UnwindIterator;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.WhereCondition;

import java.util.List;

public class UnwindIteratorImpl implements UnwindIterator {

    protected final CypherVar innerVar;
    protected final String listExpression;
    protected final List<WhereCondition> filters;
    protected final List<String> returnExpressions;

    public UnwindIteratorImpl(CypherVar innerVar, String listExpression, List<WhereCondition> filters, List<String> returnExpressions) {
        this.innerVar = innerVar;
        this.listExpression = listExpression;
        this.filters = filters;
        this.returnExpressions = returnExpressions;
    }

    public CypherVar getInnerVar() {
        return innerVar;
    }

    public String getListExpression() {
        return listExpression;
    }

    public List<WhereCondition> getFilters() {
        return filters;
    }

    public List<String> getReturnExpressions() {
        return returnExpressions;
    }
}
