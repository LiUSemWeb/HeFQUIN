package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Objects;

public class FilteredPropertiesReturnStatement {
    private final CypherVar var;
    private final String alias;
    private final String innerVar;
    private final String filterValue;

    public FilteredPropertiesReturnStatement(final CypherVar var, final String alias,
                                             final String filterValue, final String innerVar) {
        assert var != null;
        assert filterValue != null;
        this.var = var;
        this.alias = alias;
        this.innerVar = innerVar;
        this.filterValue = filterValue;
    }

    public FilteredPropertiesReturnStatement(final CypherVar var, final String alias,
                                             final String filterValue) {
        this(var, alias, filterValue, "k");
    }

    public CypherVar getVar() {
        return var;
    }

    public String getAlias() {
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
                + filterValue + "\" | pm(" + innerVar + ")]" + (alias != null ? " AS " + alias : "");
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
}
