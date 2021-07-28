package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Objects;

public class PropertyListReturnStatement {
    private final CypherVar var;
    private final String alias;
    private final String innerVar;

    public PropertyListReturnStatement(final CypherVar var, final String alias) {
        this(var, alias, "k");
    }

    public PropertyListReturnStatement(final CypherVar var, final String alias, final String innerVar) {
        assert var != null;
        this.var = var;
        this.alias = alias;
        this.innerVar = innerVar;
    }

    public CypherVar getVar() {
        return var;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return "[" + innerVar + " IN KEYS(" + var.getName() + ") | pm(" + var.getName() + ")]"
                + (alias != null? " AS "+ alias : "");
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
}
