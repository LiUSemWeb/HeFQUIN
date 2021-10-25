package se.liu.ida.hefquin.engine.query.cypher.condition;

import se.liu.ida.hefquin.engine.query.cypher.CypherVar;
import se.liu.ida.hefquin.engine.query.cypher.WhereCondition;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a condition checking that the value of a property is a given literal, with an optional alias
 * For example, n.name = "Quentin Tarantino"
 */
public class PropertyValueCondition implements WhereCondition {
    protected final CypherVar var;
    protected final String property;
    protected final String value;

    public PropertyValueCondition(CypherVar var, String property, String value) {
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
        if (!(o instanceof PropertyValueCondition)) return false;
        PropertyValueCondition that = (PropertyValueCondition) o;
        return var.equals(that.var) && property.equals(that.property) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, property, value);
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.singleton(var);
    }
}
