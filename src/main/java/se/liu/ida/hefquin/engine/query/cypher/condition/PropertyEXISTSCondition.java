package se.liu.ida.hefquin.engine.query.cypher.condition;

import se.liu.ida.hefquin.engine.query.cypher.CypherVar;
import se.liu.ida.hefquin.engine.query.cypher.WhereCondition;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an EXISTS condition over a property
 * For example, EXISTS(n.name)
 */
public class PropertyEXISTSCondition implements WhereCondition {
    private final CypherVar var;
    private final String property;

    public PropertyEXISTSCondition(final CypherVar var, final String property) {
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
        if (!(o instanceof PropertyEXISTSCondition)) return false;
        PropertyEXISTSCondition that = (PropertyEXISTSCondition) o;
        return var.equals(that.var) && property.equals(that.property);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, property);
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.singleton(var);
    }
}
