package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Objects;

public class ValueReturnStatement implements ReturnStatement{
    private final String value;
    private final String alias;

    public ValueReturnStatement(final String value, final String alias) {
        assert value != null;
        this.value = value;
        this.alias = alias;
    }

    public String getValue() {
        return value;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return value + (alias != null? " AS " + alias : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValueReturnStatement)) return false;
        ValueReturnStatement that = (ValueReturnStatement) o;
        return value.equals(that.value) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, alias);
    }
}
