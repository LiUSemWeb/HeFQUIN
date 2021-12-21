package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.condition;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.WhereCondition;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a condition that checks that the size of the list of properties with a given literal value
 * of a node or edge is larger than zero. For instance:
 *      size([k in keys(x) where x[k]="literal"]) > 0
 */
public class FilterEmptyPropertyListsCondition implements WhereCondition {

    protected final CypherVar var;
    protected final String literal;

    public FilterEmptyPropertyListsCondition(final CypherVar var, final String literal) {
        assert var != null;
        assert literal != null;
        this.var = var;
        this.literal = literal;
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.singleton(var);
    }

    public CypherVar getVar() {
        return var;
    }

    public String getLiteral() {
        return literal;
    }

    @Override
    public String toString() {
        return "SIZE([k IN KEYS("+var+") WHERE "+var+"[k]='"+literal+"']) > 0";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilterEmptyPropertyListsCondition)) return false;
        FilterEmptyPropertyListsCondition that = (FilterEmptyPropertyListsCondition) o;
        return var.equals(that.var) && literal.equals(that.literal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, literal);
    }
}
