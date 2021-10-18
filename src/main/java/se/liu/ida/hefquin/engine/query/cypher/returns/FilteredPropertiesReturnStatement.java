package se.liu.ida.hefquin.engine.query.cypher.returns;

import se.liu.ida.hefquin.engine.query.cypher.CypherVar;
import se.liu.ida.hefquin.engine.query.cypher.ReturnStatement;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a statement that returns the properties of a node or edge that have a given value, with an optional alias
 * For example, RETURN [k in KEYS(x) WHERE x[k]=value | k] AS y
 */
public class FilteredPropertiesReturnStatement implements ReturnStatement {
    private final CypherVar var;
    private final CypherVar alias;
    private final String innerVar;
    private final String filterValue;

    public FilteredPropertiesReturnStatement(final CypherVar var, final CypherVar alias,
                                             final String filterValue, final String innerVar) {
        assert var != null;
        assert filterValue != null;
        this.var = var;
        this.alias = alias;
        this.innerVar = innerVar;
        this.filterValue = filterValue;
    }

    public FilteredPropertiesReturnStatement(final CypherVar var, final CypherVar alias,
                                             final String filterValue) {
        this(var, alias, filterValue, "k");
    }

    public CypherVar getVar() {
        return var;
    }

    @Override
    public CypherVar getAlias() {
        return alias;
    }

    public String getInnerVar() {
        return innerVar;
    }

    public String getFilterValue() {
        return filterValue;
    }

    @Override
    public String toString() {
        return "[" + innerVar + " IN KEYS(" + var.getName() + ") WHERE " + var.getName() + "[" + innerVar + "]=\""
                + filterValue + "\" | " + innerVar + "]" + (alias != null ? " AS " + alias.getName() : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilteredPropertiesReturnStatement)) return false;
        FilteredPropertiesReturnStatement that = (FilteredPropertiesReturnStatement) o;
        return var.equals(that.var) && Objects.equals(alias, that.alias) && innerVar.equals(that.innerVar) && filterValue.equals(that.filterValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, alias, innerVar, filterValue);
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.singleton(var);
    }
}
