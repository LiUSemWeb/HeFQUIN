package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Objects;

public class ValueWhereCondition implements WhereCondition{
    private final CypherVar var;
    private final String property;
    private final String value;

    public ValueWhereCondition(CypherVar var, String property, String value) {
        assert var != null;
        assert property != null;
        assert value != null;
        this.var = var;
        this.property = property;
        this.value = value;
    }

    public CypherVar getVar() {
        return var;
    }

    public String getProperty() {
        return property;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return var.getName() + "." + property + "=\"" + value +"\"";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValueWhereCondition)) return false;
        ValueWhereCondition that = (ValueWhereCondition) o;
        return var.equals(that.var) && property.equals(that.property) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, property, value);
    }
}
