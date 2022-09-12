package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.returns;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.ReturnStatement;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a statement that returns the list of properties of an edge or node, with an optional alias
 * For example, RETURN KEYS(x) AS p
 */
public class PropertyListReturnStatement implements ReturnStatement {
    private final CypherVar var;
    private final CypherVar alias;

    public PropertyListReturnStatement(final CypherVar var, final CypherVar alias) {
        assert var != null;
        this.var = var;
        this.alias = alias;
    }

    public CypherVar getVar() {
        return var;
    }

    @Override
    public CypherVar getAlias() {
        return alias;
    }

    @Override
    public String getExpression() {
        return "";
    }

    @Override
    public String toString() {
        return "KEYS(" + var.getName() + ")" + (alias != null? " AS "+ alias.getName() : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyListReturnStatement)) return false;
        PropertyListReturnStatement that = (PropertyListReturnStatement) o;
        return var.equals(that.var) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, alias);
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.singleton(var);
    }
}
