package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.UnwindIterator;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.WhereCondition;

import java.util.List;

public class UnwindIteratorImpl implements UnwindIterator {

    protected final CypherVar innerVar;
    //TODO: this below should be a CypherExpression
    protected final String listExpression;
    protected final List<WhereCondition> filters;
    protected final List<String> returnExpressions;
    protected final CypherVar alias;

    public UnwindIteratorImpl(final CypherVar innerVar, final String listExpression,
                              final List<WhereCondition> filters, final List<String> returnExpressions,
                              final CypherVar alias) {
        assert innerVar != null;
        assert listExpression != null;
        assert alias != null;

        this.innerVar = innerVar;
        this.listExpression = listExpression;
        this.filters = filters;
        this.returnExpressions = returnExpressions;
        this.alias = alias;
    }

    @Override
    public CypherVar getInnerVar() {
        return innerVar;
    }

    @Override
    public String getListExpression() {
        return listExpression;
    }

    @Override
    public List<WhereCondition> getFilters() {
        return filters;
    }

    @Override
    public List<String> getReturnExpressions() {
        return returnExpressions;
    }

    @Override
    public CypherVar getAlias() {
        return alias;
    }


}
