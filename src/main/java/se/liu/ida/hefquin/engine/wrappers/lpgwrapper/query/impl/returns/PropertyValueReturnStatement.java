package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.returns;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.ReturnStatement;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a statement that returns the value of a property, with an optional alias
 * For example, RETURN n.p
 */
public class PropertyValueReturnStatement implements ReturnStatement {
    private final CypherVar var;
    private final String property;
    private final CypherVar alias;

    public PropertyValueReturnStatement(final CypherVar var, final String property, final CypherVar alias) {
        assert var != null;
        assert property != null;
        this.var = var;
        this.property = property;
        this.alias = alias;
    }

    public CypherVar getVar() {
        return var;
    }

    public String getProperty() {
        return property;
    }

    @Override
    public CypherVar getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return var.getName() + "." + property + (alias != null? " AS " + alias.getName() : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyValueReturnStatement)) return false;
        PropertyValueReturnStatement that = (PropertyValueReturnStatement) o;
        return var.equals(that.var) && property.equals(that.property) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, property, alias);
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.singleton(var);
    }
}
