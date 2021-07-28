package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Objects;

public class EXISTSWhereCondition implements WhereCondition{
    private final CypherVar var;
    private final String property;

    public EXISTSWhereCondition(final CypherVar var, final String property) {
        assert var != null;
        assert property != null;
        this.var = var;
        this.property = property;
    }

    public CypherVar getVar() {
        return var;
    }

    public String getProperty() {
        return property;
    }

    @Override
    public String toString() {
        return "EXISTS("+var.getName()+"."+property+")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EXISTSWhereCondition)) return false;
        EXISTSWhereCondition that = (EXISTSWhereCondition) o;
        return var.equals(that.var) && property.equals(that.property);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, property);
    }
}
