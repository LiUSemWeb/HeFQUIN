package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Objects;

public class PropertyValuesReturnStatement {
    private final CypherVar var;
    private final String alias;
    private final String innerVar;

    public PropertyValuesReturnStatement(final CypherVar var, final String alias) {
        this(var, alias, "k");
    }

    public PropertyValuesReturnStatement(final CypherVar var, final String alias, final String innerVar) {
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

    public String getInnerVar() {
        return innerVar;
    }

    @Override
    public String toString() {
        return "[" + innerVar + " IN KEYS(" + var.getName() + ") | " + var.getName() + "[" + innerVar + "]]"
                +(alias != null ? " AS " + alias : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyValuesReturnStatement)) return false;
        PropertyValuesReturnStatement that = (PropertyValuesReturnStatement) o;
        return var.equals(that.var) && Objects.equals(alias, that.alias) && innerVar.equals(that.innerVar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, alias, innerVar);
    }
}
