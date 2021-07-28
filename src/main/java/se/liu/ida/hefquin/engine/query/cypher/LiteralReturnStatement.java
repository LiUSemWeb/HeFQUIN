package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Objects;

public class LiteralReturnStatement implements ReturnStatement{
    private final CypherVar var;
    private final String property;
    private final String alias;

    public LiteralReturnStatement(final CypherVar var, final String property, final String alias) {
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

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return var.getName() + "." + property + (alias != null? " AS " + alias : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LiteralReturnStatement)) return false;
        LiteralReturnStatement that = (LiteralReturnStatement) o;
        return var.equals(that.var) && property.equals(that.property) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, property, alias);
    }
}
