package se.liu.ida.hefquin.engine.query.cypher.returns;

import se.liu.ida.hefquin.engine.query.cypher.CypherVar;
import se.liu.ida.hefquin.engine.query.cypher.ReturnStatement;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a statement that returns the list of properties of an edge or node, with an optional alias
 * For example, RETURN [k in KEYS(x) | k]
 */
public class PropertyListReturnStatement implements ReturnStatement {
    private final CypherVar var;
    private final CypherVar alias;
    private final String innerVar;

    public PropertyListReturnStatement(final CypherVar var, final CypherVar alias) {
        this(var, alias, "k");
    }

    public PropertyListReturnStatement(final CypherVar var, final CypherVar alias, final String innerVar) {
        assert var != null;
        this.var = var;
        this.alias = alias;
        this.innerVar = innerVar;
    }

    public CypherVar getVar() {
        return var;
    }

    @Override
    public CypherVar getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return "[" + innerVar + " IN KEYS(" + var.getName() + ") | " + innerVar + "]"
                + (alias != null? " AS "+ alias.getName() : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyListReturnStatement)) return false;
        PropertyListReturnStatement that = (PropertyListReturnStatement) o;
        return var.equals(that.var) && Objects.equals(alias, that.alias) && Objects.equals(innerVar, that.innerVar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, alias, innerVar);
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.singleton(var);
    }
}
